/*
 * Copyright (C) 2018 IRT GmbH
 *
 * Author:
 *  Fabian Sattler
 *
 * This file is a part of IRT DAB library.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

#include "dabplusservicecomponentdecoder.h"
#include "dabservicecomponent.h"
#include "fig.h"

extern "C" {
    #include "thirdparty/fec/fec.h"
}

#include <iostream>
#include <iomanip>

#include <pthread.h>
#include <unistd.h>
#include <cerrno>

DabPlusServiceComponentDecoder::DabPlusServiceComponentDecoder() {
    //std::cout << m_logTag << " Constructing" << std::endl;
    m_processThreadRunning = true;
    //m_processThread = std::thread(&DabPlusServiceComponentDecoder::processDataOld, this);
    m_processThread = std::thread(&DabPlusServiceComponentDecoder::processData, this);
}

DabPlusServiceComponentDecoder::~DabPlusServiceComponentDecoder() {
    //std::cout << m_logTag << " Deconstructing" << std::endl;

    m_processThreadRunning = false;
    if(m_processThread.joinable()) {
        m_processThread.join();
    }
    //m_conQueue.clear();
    m_audioDataDispatcher.clear();
    m_padDataDispatcher.clear();
}

void DabPlusServiceComponentDecoder::setSubchannelBitrate(uint16_t bitrate) {
    if (bitrate != DabServiceComponent::SUBCHAN_BITRATE_INVALID && bitrate != m_subChanBitrate) {
        std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
        DabServiceComponentDecoder::setSubchannelBitrate(bitrate);

        // ETSI TS 102 563 V2.1.1, 5.1 HE-AACv2 audio coding
        // Superframe size = bitrate/8*110
        // subchannel_index = MSC sub-channel size (kbps) ÷ 8
        // audio_super_frame_size (bytes) = subchannel_index × 110
        // Note: this is the size of the super frame AFTER the Read-Solomon decoder
        uint16_t prevSuperFrameSize = m_superFrameSize;
        m_superFrameSize = static_cast<uint16_t>((m_subChanBitrate / 8) * 110);

        if (m_superFrameSize != prevSuperFrameSize) {
            std::cout << m_logTag << " SubChanId: " << +getSubChannelId() << " SuperFrameSize: " << +m_superFrameSize << " SubchanBitrate: "
                      << +m_subChanBitrate << std::endl;

            resetInSync();
        }
    }
}

void DabPlusServiceComponentDecoder::resetInSync() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    m_unsyncSync = false;
    m_unsyncFrameCount = 0;
    m_unsyncByteCount = 0;
    m_unsyncDataBuffer.clear();
}

void DabPlusServiceComponentDecoder::componentDataInput(const std::vector<uint8_t> &frameData, bool synchronized) {
    if(!m_processThreadRunning || m_superFrameSize == 0 || m_frameSize == 0) {
        return;
    }

    if (synchronized) {
        m_conQueue.push(frameData);
    } else {
        synchronizeData(frameData);
    }
}

void DabPlusServiceComponentDecoder::flushBufferedData() {
    m_conQueue.clear();
}

/* ETSI TS 102 563 V2.1.1 Annex C
 * Before the receiver can decode an audio super frame, it first concatenates five consecutive DAB frames
 * to build the audio super frame. The receiver needs a mechanism to be able to determine which five
 * consecutive DAB frames within a sub channel constitute an audio super frame.
 */
void DabPlusServiceComponentDecoder::synchronizeData(const std::vector<uint8_t>& unsyncData) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);

    /* NOTE: There is nothing left for optimization here !! */

    std::vector<uint8_t> data;

    auto unsyncDataBufferSize = m_unsyncDataBuffer.size();
    if(unsyncDataBufferSize > 0) {
        // prepend any left over data from previous call
        data.insert(data.begin(), m_unsyncDataBuffer.begin(), m_unsyncDataBuffer.end());

        // only for housekeeping
        if (m_unsyncByteCount >= unsyncDataBufferSize) {
            // don't calculate these bytes twice
            m_unsyncByteCount -= unsyncDataBufferSize;
        } // else: m_unsyncByteCount was reset to 0 from "outside"

        // clear old data
        m_unsyncDataBuffer.clear();
    }

    // add new data coming in
    data.insert(data.end(), unsyncData.begin(), unsyncData.end());

    auto unsyncIter = data.begin();
    while (unsyncIter + m_frameSize < data.end()) {
        if (m_unsyncSync) {
            while (unsyncIter + m_frameSize < data.end()) {
                //std::cout << m_logTag << "synchronizeData push: " << Fig::toHexString(std::vector<uint8_t>(unsyncIter, unsyncIter + 6)) << std::endl;
                m_conQueue.push(std::vector<uint8_t>(unsyncIter, unsyncIter + m_frameSize));
                unsyncIter += m_frameSize;
                ++m_unsyncFrameCount;
                // mandatory: every 5 frames, check again if we are still in sync
                if (m_unsyncFrameCount == NUM_FRAMES_PER_SUPER_FRAME) {
                    m_unsyncSync = false;
                    m_unsyncFrameCount = 0;
                    break;
                }
            }
            continue;
        }

        /* A first test is used to check if the first DAB frame in the audio super frame buffer
         * might be the beginning of an audio super frame.
         */
        std::vector<uint8_t> checkData(unsyncIter, unsyncIter + m_frameSize);

        /* The receiver does the RS decoding to correct all reception errors (if there were any) */

        // This causes enormous stuttering !
        // m_rsDec.DecodeSuperFrame(checkData.data(), checkData.size());

        /* and then checks whether the header checksum is correct; for this test, the Fire code
         * is used for error detection only.
         * If that is the case, then the first valid audio super frame was received.
         * Synchronization is completed.
         */
        bool dacRate, sbr, chanMode, ps;
        uint8_t numAUs;
        std::vector<uint16_t> auStarts, auLengths;
        if (decodeSuperFrameHeader(checkData, m_superFrameSize,
                                   dacRate, sbr, chanMode, ps,
                                   numAUs, auStarts, auLengths, false /* don't log errors */)) {
            if (CHECK_FIRECODE(checkData.data(), checkData.size())) {
                // Ideally, there is one super frame following the other with no single byte in between
                if (m_unsyncByteCount > 0) {
                    std::stringstream logStr;
                    logStr << m_logTag << " SubChanId " << +getSubChannelId()
                           << ": Start of super frame after "
                           << +m_unsyncByteCount << " bytes";
                    std::cout << logStr.str() << std::endl;
                }
                m_conQueue.push(checkData);
                ++m_unsyncFrameCount;
                m_unsyncSync = true; // we are in sync now
                unsyncIter += m_frameSize; // change over to frame-wise iteration
                m_unsyncByteCount = 0;
                continue;
            } // else: continue searching byte-wise
        } // else: continue searching byte-wise

        // byte-wise search for start of first frame of super frame
        unsyncIter++;
        m_unsyncByteCount++;

        if (m_unsyncByteCount % 8192 == 0) {
            std::clog << "Warning: Searching Super frame for " << +m_unsyncByteCount << " bytes" << std::endl;
        }
    }

    m_unsyncDataBuffer.insert(m_unsyncDataBuffer.begin(), unsyncIter, data.end());
}

std::shared_ptr<DabPlusServiceComponentDecoder::PAD_DATA_CALLBACK> DabPlusServiceComponentDecoder::registerPadDataCallback(DabPlusServiceComponentDecoder::PAD_DATA_CALLBACK cb) {
    return m_padDataDispatcher.add(cb);
}

std::shared_ptr<DabPlusServiceComponentDecoder::AUDIO_COMPONENT_DATA_CALLBACK> DabPlusServiceComponentDecoder::registerAudioDataCallback(DabPlusServiceComponentDecoder::AUDIO_COMPONENT_DATA_CALLBACK cb) {
    return m_audioDataDispatcher.add(cb);
}

bool DabPlusServiceComponentDecoder::combineSuperFrame(/*out*/ DabSuperFrame & superFrame,
        /*in*/ std::vector<std::vector<uint8_t>> & frameRingBuffer,
        /*in*/ uint8_t frameRingBufNextIdx) {
    superFrame.clear();

    // the next free ring buffer entry is equal to the oldest entry in ring buffer
    uint8_t idx = frameRingBufNextIdx;

    uint8_t cntDown = NUM_FRAMES_PER_SUPER_FRAME;
    while (cntDown > 0) {
        superFrame.superFrameData.insert(superFrame.superFrameData.end(),
                                         frameRingBuffer.at(idx).begin(),
                                         frameRingBuffer.at(idx).end());

        idx = (idx + 1) % NUM_FRAMES_PER_SUPER_FRAME;
        --cntDown;
    }
    return true;
}

/* ETSI TS 102 563 V2.1.1
 * 5.1 HE-AACv2 audio coding
 * 5.2 Audio super framing syntax
 * Annex D (informative): Processing a super frame
 */
void DabPlusServiceComponentDecoder::processData() {
    const char threadname[] = "DplusProcessData";
    pthread_setname_np(pthread_self(), threadname);

    const int THREAD_PRIORITY_AUDIO = -16; // android.os.Process.THREAD_PRIORITY_AUDIO
    int newprio = nice(THREAD_PRIORITY_AUDIO);
    if (newprio == -1) {
        int lErrno = errno;
        std::clog << m_logTag << "nice failed: " << strerror(lErrno) << std::endl;
    }

    // Super frame
    DabSuperFrame superFrame;

    // ring buffer of last 5 frames that came in
    std::vector<std::vector<uint8_t>> frameRingBuf(NUM_FRAMES_PER_SUPER_FRAME);
    // 0..5, counts how many frames are available in ring buffer
    uint8_t dabSuperFrameCount = 0;
    // 0..4, index of next free ring buffer slot
    uint8_t frameRingBufNextIdx = 0;

    // the current incoming frame
    std::vector<uint8_t> frameData;

    while(m_processThreadRunning) {
        frameData.clear();

        // 1. pop a frame from the input queue
        // 2. insert frame into a ring buffer slot (0..4), overwriting any previous data
        // 3. combine a potential super frame using the 5 frames (oldest frame first)
        // 4. perform Read-Solomon decoder
        // 5. check Fire code
        // 6. if Fire code OK, pull AAC audio frames out of the super frame
        // 7. if Fire code NOK, wait for next frame

        if (m_conQueue.tryPop(frameData, std::chrono::milliseconds(50))) {
            if (dabSuperFrameCount >= NUM_FRAMES_PER_SUPER_FRAME) {
                std::cout << m_logTag << " frame overflow: " << +dabSuperFrameCount << std::endl;
            }
            // copy this frame into next free space in ring buffer
            std::vector<uint8_t> & currRingBufEntry = frameRingBuf.at(frameRingBufNextIdx);
            currRingBufEntry.clear();
            currRingBufEntry.insert(currRingBufEntry.begin(),
                                    frameData.begin(),  frameData.end());
            // one more frame in ring buffer
            dabSuperFrameCount++;
            // increase index into ring buffer for the next frame
            frameRingBufNextIdx = (frameRingBufNextIdx + 1) % NUM_FRAMES_PER_SUPER_FRAME;

            if (dabSuperFrameCount == NUM_FRAMES_PER_SUPER_FRAME)
            {
                if (combineSuperFrame(/*out*/ superFrame,
                        /*in*/ frameRingBuf, /*in*/ frameRingBufNextIdx))
                {
                    if (!m_rsDec.DecodeSuperFrame(superFrame.superFrameData.data(),
                                                  superFrame.superFrameData.size())) {
                        // uncorrectable errors
                        std::cout << m_logTag << " Super frame uncorr errors" << std::endl;
                        dabSuperFrameCount = 0;
                        continue;
                    }
                } else {
                    // should never happen
                    std::clog << m_logTag << " combine Super frame failed" << std::endl;
                    dabSuperFrameCount = 0;
                    continue;
                }
            } else {
                // not enough frames yet
                continue;
            }
        } else {
            // tryPop timed out
            continue;
        }

        if (dabSuperFrameCount != NUM_FRAMES_PER_SUPER_FRAME) {
            // house keeping went wrong
            std::clog << m_logTag << " Super frame count wrong: " << +dabSuperFrameCount << std::endl;
            dabSuperFrameCount = 0; // start all over
            continue;
        }

        /* ********************************************************************
         * if we come here, then we have a valid audio super frame in superFrame.superFrameData,
         * but super frame header is not decoded yet
         * and Read-Solomon decoder has not run yet
         * note: every failure after this point shall cause a resynchronization to super frame
         */
        bool dacRate, sbr, chanMode, ps;
        if (decodeSuperFrameHeader(superFrame.superFrameData, m_superFrameSize,
                            dacRate, sbr, chanMode, ps,
                            superFrame.numAUs,
                            superFrame.auStarts,
                            superFrame.auLengths, true /* log errors */)) {
            superFrame.sbrUsed = sbr;
            superFrame.psUsed = ps;
            superFrame.channels = chanMode ? 2 : 1;
            superFrame.samplingRate = dacRate ? 48000 : 32000;
        } else {
            superFrame.clear();
            dabSuperFrameCount = 0;
            continue;
        }

        // check for a wish to stop this thread, lengthy processing will come next
        if (!m_processThreadRunning) {
            goto stop_thread;
        }

        if (!CHECK_FIRECODE(superFrame.superFrameData.data(), superFrame.superFrameData.size())) {
            // fire code check failure
            std::cout << m_logTag << " Super frame Fire code wrong" << std::endl;
            dabSuperFrameCount = 0;
            continue;
        }

        // Assemble all access units (AU) from super frame
        std::vector<uint8_t> audioBuff;
        for (int i = 0; i < superFrame.numAUs; i++) {

            // check for a wish to stop this thread, lengthy processing will come next
            if (!m_processThreadRunning) {
                goto stop_thread;
            }

            if (!CRC_CCITT_CHECK(superFrame.superFrameData.data() +
                                superFrame.auStarts[i],
                                superFrame.auLengths[i]))
            {
                std::clog << m_logTag << " SuperFrame AU[" << +i
                          << "] CRC failed, Bitrate: " << +m_subChanBitrate << std::endl;
                // resynchronize to Super frame
                dabSuperFrameCount = 0; // start all over
                continue;
            }


            if (((static_cast<unsigned>(superFrame.superFrameData[superFrame.auStarts[i]])
                    >> 5u) & 0x07u) == 0x04u) {
                uint8_t padDataStart = 2;
                uint8_t padDataLen = superFrame.superFrameData[
                        superFrame.auStarts[i] + 1];
                if (padDataLen == 0xFF) {
                    padDataLen += superFrame.superFrameData[
                            superFrame.auStarts[i] + 2];
                    ++padDataStart;
                }

                std::vector<uint8_t> padData(
                        superFrame.superFrameData.begin() +
                        superFrame.auStarts[i] + padDataStart,
                        superFrame.superFrameData.begin() +
                        superFrame.auStarts[i] + padDataStart +
                        padDataLen);

                if (!m_processThreadRunning) {
                    goto stop_thread;
                }
                m_padDataDispatcher.invoke(padData);

                //change the beginnings and lengths of the AU
                superFrame.auStarts[i] += (padDataStart + padDataLen);
                superFrame.auLengths[i] -= (padDataStart + padDataLen);
            }

            audioBuff.insert(audioBuff.cend(),
                             superFrame.superFrameData.begin() +
                             superFrame.auStarts[i],
                             superFrame.superFrameData.begin() +
                             superFrame.auStarts[i] +
                             superFrame.auLengths[i] -
                             2); // -2 of length to cut off CRC
        }

        // check for a wish to stop this thread, lengthy processing will come next
        if (!m_processThreadRunning) {
            goto stop_thread;
        }
        m_audioDataDispatcher.invoke(audioBuff, 63, superFrame.channels,
                                     superFrame.samplingRate, superFrame.sbrUsed,
                                     superFrame.psUsed);

        // Done. All 5 frames forming a super frame processed
        dabSuperFrameCount = 0;
    }

    //std::cout << m_logTag << " ProcessData thread stopped" << std::endl;
    return;

    stop_thread:
    std::cout << m_logTag << " ProcessData thread stopped quickly" << std::endl;
}

void DabPlusServiceComponentDecoder::clearCallbacks() {
    DabServiceComponentDecoder::clearCallbacks();
    //std::cout << m_logTag << " clearCallbacks" << std::endl;
    m_audioDataDispatcher.clear();
    m_padDataDispatcher.clear();
}

constexpr uint16_t DabPlusServiceComponentDecoder::FIRECODE_TABLE[256] = {
    0x0000, 0x782f, 0xf05e, 0x8871, 0x9893, 0xe0bc, 0x68cd, 0x10e2,
    0x4909, 0x3126, 0xb957, 0xc178, 0xd19a, 0xa9b5, 0x21c4, 0x59eb,
    0x9212, 0xea3d, 0x624c, 0x1a63, 0x0a81, 0x72ae, 0xfadf, 0x82f0,
    0xdb1b, 0xa334, 0x2b45, 0x536a, 0x4388, 0x3ba7, 0xb3d6, 0xcbf9,
    0x5c0b, 0x2424, 0xac55, 0xd47a, 0xc498, 0xbcb7, 0x34c6, 0x4ce9,
    0x1502, 0x6d2d, 0xe55c, 0x9d73, 0x8d91, 0xf5be, 0x7dcf, 0x05e0,
    0xce19, 0xb636, 0x3e47, 0x4668, 0x568a, 0x2ea5, 0xa6d4, 0xdefb,
    0x8710, 0xff3f, 0x774e, 0x0f61, 0x1f83, 0x67ac, 0xefdd, 0x97f2,
    0xb816, 0xc039, 0x4848, 0x3067, 0x2085, 0x58aa, 0xd0db, 0xa8f4,
    0xf11f, 0x8930, 0x0141, 0x796e, 0x698c, 0x11a3, 0x99d2, 0xe1fd,
    0x2a04, 0x522b, 0xda5a, 0xa275, 0xb297, 0xcab8, 0x42c9, 0x3ae6,
    0x630d, 0x1b22, 0x9353, 0xeb7c, 0xfb9e, 0x83b1, 0x0bc0, 0x73ef,
    0xe41d, 0x9c32, 0x1443, 0x6c6c, 0x7c8e, 0x04a1, 0x8cd0, 0xf4ff,
    0xad14, 0xd53b, 0x5d4a, 0x2565, 0x3587, 0x4da8, 0xc5d9, 0xbdf6,
    0x760f, 0x0e20, 0x8651, 0xfe7e, 0xee9c, 0x96b3, 0x1ec2, 0x66ed,
    0x3f06, 0x4729, 0xcf58, 0xb777, 0xa795, 0xdfba, 0x57cb, 0x2fe4,
    0x0803, 0x702c, 0xf85d, 0x8072, 0x9090, 0xe8bf, 0x60ce, 0x18e1,
    0x410a, 0x3925, 0xb154, 0xc97b, 0xd999, 0xa1b6, 0x29c7, 0x51e8,
    0x9a11, 0xe23e, 0x6a4f, 0x1260, 0x0282, 0x7aad, 0xf2dc, 0x8af3,
    0xd318, 0xab37, 0x2346, 0x5b69, 0x4b8b, 0x33a4, 0xbbd5, 0xc3fa,
    0x5408, 0x2c27, 0xa456, 0xdc79, 0xcc9b, 0xb4b4, 0x3cc5, 0x44ea,
    0x1d01, 0x652e, 0xed5f, 0x9570, 0x8592, 0xfdbd, 0x75cc, 0x0de3,
    0xc61a, 0xbe35, 0x3644, 0x4e6b, 0x5e89, 0x26a6, 0xaed7, 0xd6f8,
    0x8f13, 0xf73c, 0x7f4d, 0x0762, 0x1780, 0x6faf, 0xe7de, 0x9ff1,
    0xb015, 0xc83a, 0x404b, 0x3864, 0x2886, 0x50a9, 0xd8d8, 0xa0f7,
    0xf91c, 0x8133, 0x0942, 0x716d, 0x618f, 0x19a0, 0x91d1, 0xe9fe,
    0x2207, 0x5a28, 0xd259, 0xaa76, 0xba94, 0xc2bb, 0x4aca, 0x32e5,
    0x6b0e, 0x1321, 0x9b50, 0xe37f, 0xf39d, 0x8bb2, 0x03c3, 0x7bec,
    0xec1e, 0x9431, 0x1c40, 0x646f, 0x748d, 0x0ca2, 0x84d3, 0xfcfc,
    0xa517, 0xdd38, 0x5549, 0x2d66, 0x3d84, 0x45ab, 0xcdda, 0xb5f5,
    0x7e0c, 0x0623, 0x8e52, 0xf67d, 0xe69f, 0x9eb0, 0x16c1, 0x6eee,
    0x3705, 0x4f2a, 0xc75b, 0xbf74, 0xaf96, 0xd7b9, 0x5fc8, 0x27e7
};

// The Fire code word shall be calculated over the nine bytes from byte 2 to byte 10 of the audio super frame.
// NOTE 1: Except in the case where num_aus = 6, the Fire code calculation will include some bytes from the first AU.
bool DabPlusServiceComponentDecoder::CHECK_FIRECODE(const uint8_t* const frameData, size_t size) {
    if (frameData == nullptr || size < NUM_BYTES_FOR_FIRE_CODE_CHECK) return false;

    uint16_t firstState = (frameData[2] << 8) | frameData[3];
    uint16_t secState;

    for (int16_t i = 4; i < NUM_BYTES_FOR_FIRE_CODE_CHECK; i++) {
        secState = FIRECODE_TABLE[firstState >> 8];
        firstState = (uint16_t) (((secState & 0x00ff) ^ frameData[i]) |
                                 ((secState ^ firstState << 8) & 0xff00));
    }

    for (int16_t i = 0; i < 2; i++) {
        secState = FIRECODE_TABLE[firstState >> 8];
        firstState = (uint16_t) (((secState & 0x00ff) ^ frameData[i]) |
                                 ((secState ^ firstState << 8) & 0xff00));
    }

    return firstState == 0;
}

bool DabPlusServiceComponentDecoder::decodeSuperFrameHeader(const std::vector<uint8_t> & superFrameData,
                                                            const uint16_t superFrameSize, /* != superFrameData.size() !! */
                                                            bool & dacRate,
                                                            bool & sbr,
                                                            bool & chanMode,
                                                            bool & ps,
                                                            uint8_t & numAUs,
                                                            std::vector<uint16_t> & auStarts,
                                                            std::vector<uint16_t> & auLengths,
                                                            bool verbose) const {
    if (superFrameData.size() < 3) {
        return false; // shortest syntactically correct header has 3 bytes
    }

    // 2 byte Fire code skipped
    // unused: bool rfa = (superFrameData[2] >> 7) & 0xFF;
    // dacRate ? samplingRate(48000) : samplingRate(32000)
    dacRate = static_cast<bool>((superFrameData[2] & 0x40u) >> 6u);
    // sbr = 0: The sampling rate of the AAC core is equal to the sampling rate of the DAC
    // sbr = 1: The sampling rate of the AAC core is half the sampling rate of the DAC
    sbr = static_cast<bool>((superFrameData[2] & 0x20u) >> 5u);
    // chanMode ? aacStereo : aacMono
    chanMode = static_cast<bool>((superFrameData[2] & 0x10u) >> 4u);
    // ps ? psUsed : psNotUsed
    ps = static_cast<bool>((superFrameData[2] & 0x08u) >> 3u);
    if (ps && !sbr && !chanMode) {
        // ps = 1: only permitted when sbr_flag == 1 && aac_channel_mode == 0
        if (verbose) {
            std::clog << m_logTag << " Bad audio parameters ps=1,sbr=0,chanMode=0" << std::endl;
        }
        return false;
    }
    // unused: auto mpgSurCfg = static_cast<uint8_t>(superFrameData[2] & 0x07u);
    // unused: bool alignmentPresent = (!(dacRate && sbr)); // 4 bits
    // until here the header has 16+1+1+1+1+1+3 = 24 bits = 3 bytes, i.e. superFrameData[0..2]
    // next byte to read is superFrameData[3]
    auto frameIter = superFrameData.cbegin() + 3;

    // AAC CoreSamplingRates
    // 16 kHz AAC core sampling rate with SBR enabled : 2 AUs
    // The first AU always starts immediately after the super frame header.
    // au_start is coded with 12 bits
    uint16_t auStart;
    auStarts.clear(); auLengths.clear();
    if (!dacRate && sbr) {
        numAUs = 2;
        // First AU starts at superFrameData[5],
        // superFrameData[3..4] contain au_start of AU #2; 4 bits padding
        if (superFrameData.size() >= 6) {
            auStarts.push_back(5);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
        } else {
            if (verbose) {
                std::cout << m_logTag << " Super frame too short for numAUs " << +numAUs
                          << ", size " << superFrameData.size() << std::endl;
            }
            return false;
        }
    }
        // 24 kHz AAC core sampling rate with SBR enabled : 3 AUs
    else if (dacRate && sbr) {
        numAUs = 3;
        // First AU starts at superFrameData[6],
        //  superFrameData[3..4] = au_start of AU #2
        //  superFrameData[4..5] = au_start of AU #3
        if (superFrameData.size() >= 7) {
            auStarts.push_back(6);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0x0Fu) << 8u | (*frameIter++ & 0xFFu));
            auStarts.push_back(auStart);
        } else {
            if (verbose) {
                std::cout << m_logTag << " Super frame too short for numAUs " << +numAUs
                          << ", size " << superFrameData.size() << std::endl;
            }
            return false;
        }
    }
        // 32 kHz AAC core sampling rate : 4 AUs
    else if (!dacRate && !sbr) {
        numAUs = 4;
        // First AU starts at superFrameData[8],
        //  superFrameData[3..4] = au_start of AU #2
        //  superFrameData[4..5] = au_start of AU #3
        //  superFrameData[6..7] = au_start of AU #4; 4 bits padding
        if (superFrameData.size() >= 9) {
            auStarts.push_back(8);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0x0Fu) << 8u | (*frameIter++ & 0xFFu));
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
        } else {
            if (verbose) {
                std::cout << m_logTag << " Super frame too short for numAUs " << +numAUs
                          << ", size " << superFrameData.size() << std::endl;
            }
            return false;
        }
    }
        // 48 kHz AAC core sampling rate : 6 AUs
    else if (dacRate && !sbr) {
        numAUs = 6;
        // First AU starts at superFrameData[11],
        //  superFrameData[3..4] = au_start of AU #2
        //  superFrameData[4..5] = au_start of AU #3
        //  superFrameData[6..7] = au_start of AU #4
        //  superFrameData[7..8] = au_start of AU #5
        //  superFrameData[9..10] = au_start of AU #6
        if (superFrameData.size() >= 12) {
            auStarts.push_back(11);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0x0Fu) << 8u | (*frameIter++ & 0xFFu));
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0xFFu) << 4u |
                                            (*frameIter & 0xF0u) >> 4u);
            auStarts.push_back(auStart);
            auStart = static_cast<uint16_t>((*frameIter++ & 0x0Fu) << 8u | (*frameIter++ & 0xFFu));
            auStarts.push_back(auStart);
        } else {
            if (verbose) {
                std::cout << m_logTag << " Super frame too short for numAUs " << +numAUs
                          << ", size " << superFrameData.size() << std::endl;
            }
            return false;
        }
    } else { // actually not reachable code (hopefully)
        if (verbose) {
            std::clog << m_logTag << " Bad audio parameters dac=" << +dacRate << " sbr="
                      << +sbr << std::endl;
        }
        return false;
    }

    //last auNum+1 for length calculation
    auStarts.push_back(superFrameSize);

    for (int i = 0; i < numAUs; i++) {
        if (auStarts[i + 1] >= auStarts[i]) {
            uint16_t lenAu = auStarts[i + 1] -
                             auStarts[i]; // note: last 2 bytes are CRC

            // the number of samples per channel per AU is 960, allow for 2 bytes CRC
            if (lenAu >= 0 && lenAu <= 962) {
                auLengths.push_back(lenAu);
            } else {
                if (verbose) {
                    std::stringstream logStr;
                    logStr << m_logTag << " Bad AU Len " << +lenAu << " at AU["
                           << +i << "] of total # AUs="
                           << +numAUs << ", total super frame bytes: "
                           << +superFrameData.size();
                    std::clog << logStr.str() << std::endl;
                }
                return false;
            }
        } else {
            if (verbose) {
                std::stringstream logStr;
                logStr << m_logTag << " Bad AU[" << +(i + 1) << "] Start="
                       << +auStarts[i + 1] << ", AU[" << +i
                       << "] Start="
                       << +auStarts[i];
                std::clog << logStr.str() << std::endl;
            }
            return false;
        }
    }

    return true;
}

// --- RSDecoder -----------------------------------------------------------------
DabPlusServiceComponentDecoder::RSDecoder::RSDecoder() : rs_packet{0}, corr_pos{0} {
    rs_handle = init_rs_char(8, 0x11D, 0, 1, 10, 135);
    if(!rs_handle)
        throw std::runtime_error("RSDecoder: error while init_rs_char");
}

DabPlusServiceComponentDecoder::RSDecoder::~RSDecoder() {
    free_rs_char(rs_handle);
}

bool DabPlusServiceComponentDecoder::RSDecoder::DecodeSuperFrame(uint8_t* sf, size_t sf_len) {
	// insert errors for test
	//sf[0] ^= 0xFF;
	//sf[10] ^= 0xFF;
	//sf[20] ^= 0xFF;

	if (sf == nullptr || sf_len == 0) return false;

    unsigned subch_index = sf_len / 120;
    unsigned total_corr_count = 0;
    bool uncorr_errors = false;

    // process all RS packets
    for(unsigned i = 0; i < subch_index; i++) {
        for(unsigned pos = 0; pos < 120; pos++)
            rs_packet[pos] = sf[pos * subch_index + i];

        // detect errors
        int corr_count = decode_rs_char(rs_handle, rs_packet, corr_pos, 0);
        if(corr_count < 0)
            uncorr_errors = true;
        else
            total_corr_count += corr_count;

        // correct errors
        for(int j = 0; j < corr_count; j++) {

            int pos = corr_pos[j] - 135;
            if(pos < 0)
                continue;

            sf[pos * subch_index + i] = rs_packet[pos];
        }
    }

    if(total_corr_count > 0 || uncorr_errors) {
        std::cout << "[RSDecoder] TotalCorrCount: " << +total_corr_count
        << " UncorrErr: " << std::boolalpha << uncorr_errors << std::noboolalpha << std::endl;
    }
    return (!uncorr_errors);
}
