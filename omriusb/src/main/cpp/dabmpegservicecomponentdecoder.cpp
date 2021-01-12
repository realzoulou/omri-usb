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

#include "dabmpegservicecomponentdecoder.h"
#include "global_definitions.h"
#include "paddecoder.h"
#include "fig.h"

#include <iomanip>

#include <pthread.h>
#include <unistd.h>
#include <cerrno>
#include <sstream>

constexpr uint8_t DabMpegServiceComponentDecoder::XPAD_SIZE[8];
constexpr uint16_t DabMpegServiceComponentDecoder::M1L2_BITRATE_IDX[];
constexpr uint16_t DabMpegServiceComponentDecoder::M1_SAMPLINGFREQUENCY_IDX[];
constexpr uint8_t DabMpegServiceComponentDecoder::CHANNELMODE_IDX[];

DabMpegServiceComponentDecoder::DabMpegServiceComponentDecoder() {
    //std::cout << m_logTag << "Creating" << std::endl;

    m_processThreadRunning = true;
#ifdef USE_ORIG_SYNC_N_PROCESSDATA
    m_processThread = std::thread(&DabMpegServiceComponentDecoder::processDataOrig, this);
#else
    m_processThread = std::thread(&DabMpegServiceComponentDecoder::processData, this);
#endif
}

DabMpegServiceComponentDecoder::~DabMpegServiceComponentDecoder() {
    //std::cout << m_logTag << "Destroying" << std::endl;

    m_processThreadRunning = false;
    if (m_processThread.joinable()) {
        m_processThread.join();
    }
    m_conQueue.clear();
}

std::shared_ptr<DabMpegServiceComponentDecoder::PAD_DATA_CALLBACK> DabMpegServiceComponentDecoder::registerPadDataCallback(DabMpegServiceComponentDecoder::PAD_DATA_CALLBACK cb) {
    return m_padDataDispatcher.add(cb);
}

std::shared_ptr<DabMpegServiceComponentDecoder::AUDIO_COMPONENT_DATA_CALLBACK> DabMpegServiceComponentDecoder::registerAudioDataCallback(DabMpegServiceComponentDecoder::AUDIO_COMPONENT_DATA_CALLBACK cb) {
    return m_audioDataDispatcher.add(cb);
}

void DabMpegServiceComponentDecoder::flushBufferedData() {
    m_conQueue.clear();
}

void DabMpegServiceComponentDecoder::componentDataInput(const std::vector<uint8_t> &frameData, bool synchronized) {
    if(!m_processThreadRunning || m_frameSize == 0) {
        return;
    }

    if(synchronized) {
        m_conQueue.push(frameData);
    } else {
        synchronizeData(frameData);
    }
}

void DabMpegServiceComponentDecoder::synchronizeData(const std::vector<uint8_t>& unsyncData) {

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
    auto frameStartIter = data.begin();
    auto frameEndIter = data.begin();

    while (unsyncIter < data.end()) {
        if (*unsyncIter == 0xFF && ((*(unsyncIter + 1)) & 0xF0u) == 0xF0) {
            frameStartIter = unsyncIter;
            unsyncIter += 2; // don't test the same 2 bytes again
            m_unsyncByteCount += 2;
            frameEndIter = unsyncIter;
            while (unsyncIter < data.end()) {
                // test for MPEG sync word, but ignore occasional 0xFFF if frame is not yet long enough
                auto frameLen = std::distance(frameStartIter, unsyncIter);
                if (*unsyncIter == 0xFF && ((*(unsyncIter + 1)) & 0xF0u) == 0xF0 && frameLen >= m_frameSize) {
                    m_conQueue.push(std::vector<uint8_t>(frameStartIter, frameEndIter));
                    frameStartIter = unsyncIter;
                    m_unsyncByteCount = 0;
                }
                // next byte in inner while loop
                ++unsyncIter; ++frameEndIter;
                ++m_unsyncByteCount;
            } // inner while loop
        } else {
            ++unsyncIter;
            ++m_unsyncByteCount;
        }
    }

    m_unsyncDataBuffer.insert(m_unsyncDataBuffer.begin(), frameStartIter, data.end());
}
#ifdef USE_ORIG_SYNC_N_PROCESSDATA
void DabMpegServiceComponentDecoder::synchronizeDataOrig(const std::vector<uint8_t>& unsyncData) {
    std::vector<uint8_t> data;
    auto unsyncDataBufferSize = m_unsyncDataBuffer.size();
    if(unsyncDataBufferSize > 0) {
        data.insert(data.begin(), m_unsyncDataBuffer.begin(), m_unsyncDataBuffer.end());

        // only for housekeeping
        if (m_unsyncByteCount >= unsyncDataBufferSize) {
            // don't calculate these bytes twice
            m_unsyncByteCount -= unsyncDataBufferSize;
        } // else: m_unsyncByteCount was reset to 0 from "outside"

        m_unsyncDataBuffer.clear();
    }

    data.insert(data.end(), unsyncData.begin(), unsyncData.end());

    auto unsyncIter = data.begin();
    while(unsyncIter+m_frameSize < data.end()) {
        if(*unsyncIter == 0xFF && ((*(unsyncIter+1)) & 0xF0) == 0xF0) {
            //Test for ID bit (not very elegant)
            if(!m_frameSizeAdjusted && ((*(unsyncIter+1)) & 0x08) != 0x08) {
                std::cout << m_logTag << "Adjusting framesize for 24 kHz samplerate" << std::endl;
                m_frameSize *= 2;
                m_frameSizeAdjusted = true;
            }
            //std::cout << m_logTag << "Syncing Inserting: " << +std::distance(data.begin(), unsyncIter) << " from: " << +data.size() << std::endl;
            if(unsyncIter + m_frameSize < data.end()) {
                if (m_unsyncByteCount > m_frameSize) { // don't log the good case
                    std::stringstream logStr;
                    logStr << m_logTag << " ORIG CODE: SubChanId " << +getSubChannelId()
                           << ": MPEG sync word after "
                           << +m_unsyncByteCount << " bytes (frame size "
                           << m_frameSize << ")";
                    std::cout << logStr.str() << std::endl;
                }
                m_conQueue.push(std::vector<uint8_t>(unsyncIter, unsyncIter+m_frameSize));
                unsyncIter += m_frameSize;
                m_unsyncByteCount = 0;
                //std::cout << m_logTag << "Syncing Next: " << std::hex << std::setfill('0') << std::setw(2) << +(*unsyncIter) << std::hex << std::setfill('0') << std::setw(2) << +(*(unsyncIter+1)) << std::dec << std::endl;
                continue;
            } else {
                //std::cout << m_logTag << "Syncing Not enough data left: " << +unsyncData.size() << std::endl;
                break;
            }
        }
        //std::cout << m_logTag << "Searching MPEG start" << std::endl;
        ++unsyncIter;
        ++m_unsyncByteCount;
    }

    m_unsyncDataBuffer.insert(m_unsyncDataBuffer.begin(), unsyncIter, data.end());
}
#endif // USE_ORIG_SYNC_N_PROCESSDATA

void DabMpegServiceComponentDecoder::processData() {
    const char threadname[] = "MpegProcessData";
    const int THREAD_PRIORITY_AUDIO = -16; // android.os.Process.THREAD_PRIORITY_AUDIO
    pthread_setname_np(pthread_self(), threadname);
    int newprio = nice(THREAD_PRIORITY_AUDIO);
    if (newprio == -1) {
        int lErrno = errno;
        std::clog << m_logTag << "nice failed: " << strerror(lErrno) << std::endl;
    }
    
    while(m_processThreadRunning) {
        std::vector<uint8_t> frameData;
        if(m_conQueue.tryPop(frameData, std::chrono::milliseconds(50))) {
            auto frameIter = frameData.begin();
            while(frameIter < frameData.end()) {
                if(*frameIter++ == 0xFF && (*frameIter & 0xF0u) == 0xF0u) {
                    // https://en.wikipedia.org/wiki/MPEG_elementary_stream#General_layout_of_MPEG-1_audio_elementary_stream
                    const auto mpegAudioVersionId = static_cast<uint8_t>(((*frameIter & 0x08u) >> 3u) & 0x01u);
                    const auto mpegAudioLayer = static_cast<uint8_t>(((*frameIter & 0x06u) >> 1u) & 0x03u);
                    const bool crcProtection = (*frameIter++ & 0x01u) != 0;

                    const auto bitrateIdx = static_cast<uint8_t>(((*frameIter & 0xF0u) >> 4u) & 0x0Fu);

                    uint16_t bitrate;
                    if(bitrateIdx <= M1L2_BITRATE_IDX_MAX) {
                        bitrate = M1L2_BITRATE_IDX[bitrateIdx];
                    } else {
                        std::clog << m_logTag << "bitrateIdx not supported:" << +bitrateIdx << std::endl;
                        break; // skip this whole frame
                    }

                    const auto samplingIdx = static_cast<uint8_t>(((*frameIter & 0x0Cu) >> 2u) & 0x03u);
                    uint16_t samplingRate;

                    if(samplingIdx <= M1_SAMPLINGFREQUENCY_IDX_MAX) {
                        samplingRate = M1_SAMPLINGFREQUENCY_IDX[samplingIdx];
                        if(mpegAudioVersionId == 0) { // MPEG 2
                            samplingRate /= 2;
                        }
                    } else {
                        std::cout << m_logTag << "samplingIdx not supported:" << +samplingIdx << std::endl;
                        break; // skip this whole frame
                    }

                    const bool paddingFlag = (((*frameIter & 0x02u) >> 1u) & 0x01u) != 0;
                    const bool privateFlag = (*frameIter++ & 0x01u) != 0;

                    const auto channelMode = static_cast<uint8_t>(((*frameIter & 0xC0u) >> 6u) & 0x03u);
                    auto channels = CHANNELMODE_IDX[0];
                    if(channelMode <= CHANNELMODE_IDX_MAX) {
                        channels = CHANNELMODE_IDX[channelMode];
                    } else {
                        std::cout << m_logTag << "channelMode not supported:" << +channelMode << std::endl;
                        break; // skip this whole frame
                    }

                    //std::cout << m_logTag << " ID: " << +mpegAudioVersionId << " SamplingIdx: " << +samplingIdx << " SampleRate: " << samplingRate << " Channels: " << +channels << std::endl;

                    const auto modeExtension = static_cast<uint8_t>(((*frameIter & 0x30u) >> 4u) & 0x03u);
                    const bool copyRight = (((*frameIter & 0x08u) >> 3u) & 0x01u) != 0;
                    const bool original = (((*frameIter & 0x04u) >> 2u) & 0x01u) != 0;
                    const auto emphasis = static_cast<uint8_t>(*frameIter++ & 0x03u);

                    uint8_t scaleCrcLen;
                    if (channelMode == 0x03) { // single channel mode
                        if(bitrateIdx > 0x02 ) { // >= 56 kbit/s
                            scaleCrcLen = 4;
                        } else {
                            scaleCrcLen = 2;
                        }
                    } else { // all other dual channel modes
                        if (bitrateIdx > 0x06) { // >= 112 kbits/s
                            scaleCrcLen = 4;
                        } else {
                            scaleCrcLen = 2;
                        }
                    }

                    PadDecoder::X_PAD_INDICATOR xPadIndicator = PadDecoder::INVALID; // becomes valid only if F-PAD type = 0
                    auto padRiter = frameData.rbegin();
                    while (padRiter < frameData.rend()) {
                        //F-PAD
                        const bool ciPresent = (((*padRiter & 0x02u) >> 1u) & 0x01u) != 0;
                        const bool z = (*padRiter & 0x01u) != 0;

                        ++padRiter; //Byte L data field
                        const auto fPadType = static_cast<uint8_t>(((*padRiter & 0xC0u) >> 6u) & 0x03u); //Byte L-1 data field
                        xPadIndicator = static_cast<PadDecoder::X_PAD_INDICATOR>(((*padRiter++ & 0x30u) >> 4u) & 0x03u);

                        if(fPadType == 0 || fPadType == 2) { // "10" = old DAB 1.4.1 only, "01" and "11" = RFU
                            padRiter += scaleCrcLen;
                            auto scaleRiterPos = padRiter;

                            switch (xPadIndicator) {
                                case PadDecoder::NO_XPAD: {
                                    // X-PAD indicator = No X-PAD
                                    m_noCiLastLength = 0; // reset previously stored length of variable X-PAD
                                    break;
                                }
                                case PadDecoder::SHORT_XPAD: {
                                    // X-PAD indicator = Short X-PAD
                                    if (fPadType == 0) {
                                        // 4 bytes X-PAD, skip Scale Factor CRC, 2 bytes F-PAD
                                        std::vector<uint8_t> padData;
                                        padData.insert(padData.end(),
                                                       frameData.end() - PadDecoder::FPAD_LEN - scaleCrcLen - PadDecoder::SHORT_XPAD_LEN,
                                                       frameData.end() - PadDecoder::FPAD_LEN - scaleCrcLen);
                                        padData.insert(padData.end(), frameData.end() - PadDecoder::FPAD_LEN,
                                                       frameData.end());

                                        m_padDataDispatcher.invoke(padData);
                                    } else {
                                        // old DAB 1.4.1 can be safely ignored
                                    }
                                    m_noCiLastLength = 0; // reset previously stored length of variable X-PAD
                                      break;
                                }
                                case PadDecoder::VARIABLE_XPAD: {
                                    // X-PAD indicator = variable size X-PAD
                                    if (fPadType == 0) {
                                        std::vector<uint8_t> padData;
                                        if (ciPresent) {
                                            m_noCiLastLength = 0; // reset previously stored length of variable X-PAD

                                            /* The contents indicators are 1 byte long.
                                             * The contents indicator list shall comprise up to 4 bytes,
                                             * thereby allowing up to four X-PAD data sub-fields within one X-PAD field.
                                             */
                                            // list of up to 4 contents indicator
                                            for (int i = 0; i < PadDecoder::VAR_XPAD_MAX_CI; i++) {
                                                const auto xpadLengthIndex = static_cast<uint8_t>((*padRiter & 0xE0u) >> 5u);
                                                const auto xpadAppType = static_cast<uint8_t>(*padRiter++ & 0x1fu);

                                                ++m_noCiLastLength;

                                                /* If the contents indicator list is shorter than four bytes, an end marker,
                                                 * consisting of a contents indicator of application type 0, shall be used
                                                 * to terminate the list */
                                                if(xpadAppType == 0) {
                                                    break;
                                                }

                                                m_noCiLastLength += XPAD_SIZE[xpadLengthIndex];
                                            }

                                            padRiter += (m_noCiLastLength -1);

                                            //  variable amount bytes X-PAD, skip Scale Factor CRC, 2 bytes F-PAD
                                            padData.insert(padData.end(), frameData.end() - 1 - PadDecoder::FPAD_LEN - scaleCrcLen - m_noCiLastLength, frameData.end() - PadDecoder::FPAD_LEN - scaleCrcLen);
                                            padData.insert(padData.end(), frameData.end()-PadDecoder::FPAD_LEN, frameData.end());

                                            m_padDataDispatcher.invoke(padData);
                                        } else { // !ciPresent
                                            /* 7.4.2.2 Variable size X-PAD
                                             * The contents indicator flag, transported in the F-PAD field, shall signal for each DAB frame,
                                             * whether the X-PAD field contains contents indicators or not.
                                             *
                                             * The contents indicator list may be omitted if both of the following conditions apply:
                                             * - the length of the X-PAD field is the same as in the previous DAB audio frame;
                                             * - the X-PAD field comprises a single data sub-field containing a continuation of the X-PAD data group or the
                                             *   byte stream carried in the last (logical meaning) X-PAD data sub-field of the previous DAB audio frame
                                             *   (i.e. data for the same user application)
                                             */
                                            if (m_noCiLastLength > 0) {
                                                // m_noCiLastLength bytes X-PAD, skip Scale Factor CRC, 2 bytes F-PAD
                                                padData.insert(padData.end(), frameData.end()-PadDecoder::FPAD_LEN-scaleCrcLen-m_noCiLastLength, frameData.end()-PadDecoder::FPAD_LEN-scaleCrcLen);
                                                padData.insert(padData.end(), frameData.end()-PadDecoder::FPAD_LEN, frameData.end());

                                                m_padDataDispatcher.invoke(padData);            
                                            }
                                        }
                                    } else {
                                        // old DAB 1.4.1 can be safely ignored
                                    }
                                    break;
                                }
                                case PadDecoder::RFU: {
                                    if (fPadType == 0) {
                                        // X-PAD indicator = reserved for future use
                                        std::clog << m_logTag << "xPadIndicator RFU" << std::endl;
                                    } else {
                                        // F-PAD type extension = serial command channel (continuation)
                                        // old DAB 1.4.1 ignored
                                    }
                                    break;
                                }
                                default: {
                                    // if we come here, we decoded X-PAD indicator wrong
                                    std::clog << m_logTag << "xPadIndicator decoding error:"
                                              << +xPadIndicator << std::endl;
                                }
                                break;
                            }

                        } else {
                            std::clog << m_logTag << "Wrong FPAD Type: " << +fPadType << std::endl;
                        }

                        break; // while (padRiter...) because there is only 1 PAD per audio frame
                    }

                    /*
                    std::stringstream logmsg;
                    logmsg << "MPEG " << (mpegAudioVersionId ? "1":"2")
                           << " L" << ((mpegAudioLayer==1) ? "3" : (mpegAudioLayer==2) ? "2" : (mpegAudioLayer==3) ? "1" : "?")
                           << ",crc=" << !crcProtection
                           << ",bitrate=" << bitrate << "kbits/s"
                           << ",samplerate=" << +samplingRate
                           << ",mode=" << ((channelMode==0) ? "stereo" : (channelMode==1) ? "joint stereo" : (channelMode==2) ? "dual" : (channelMode==3) ? "single" : "?")
                           << ",framelen=" << +frameData.size()
                           << ",xPadInd=" << ((xPadIndicator==PadDecoder::NO_XPAD) ? "NO" :
                            (xPadIndicator==PadDecoder::SHORT_XPAD) ? "SHORT" :
                            (xPadIndicator==PadDecoder::VARIABLE_XPAD) ? "VAR" :
                            (xPadIndicator==PadDecoder::RFU) ? "RFU" : "?")
                           << ",noCiLastXpadLen=" << +m_noCiLastLength
                           << ",scfcrclen=" << +scaleCrcLen;
                    std::cout << m_logTag << logmsg.str() << std::endl;
                    */

                    // always dispatch audio, even if PAD was not decoded
                    m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false, false);

                } else {
                    if (frameData.size() >= 2) {
                        std::clog << m_logTag << "Wrong MPEG Syncword " << std::hex << +frameData[0]
                                  << ":" << +frameData[1] << std::dec << std::endl;
                    } else {
                        std::clog << m_logTag << "Wrong MPEG Syncword" << std::endl;
                    }
                }
                break; // while (frameIter...) because there is only 1 audio frame
            }
        }
    }
}
#ifdef USE_ORIG_SYNC_N_PROCESSDATA
void DabMpegServiceComponentDecoder::processDataOrig() {
    while(m_processThreadRunning) {
        std::vector<uint8_t> frameData;
        if(m_conQueue.tryPop(frameData, std::chrono::milliseconds(50))) {
            auto frameIter = frameData.begin();
            while(frameIter < frameData.end()) {
                if(*frameIter++ == 0xFF && (*frameIter & 0xF0) == 0xF0) {
                    uint8_t mpegAudioVersionId = static_cast<uint8_t>((*frameIter & 0x08) >> 3);
                    uint8_t mpegAudioLayer = static_cast<uint8_t>((*frameIter & 0x06) >> 1);
                    bool crcProtection = (*frameIter++ & 0x01) != 0;

                    uint8_t bitrateIdx = static_cast<uint8_t>((*frameIter & 0xF0) >> 4);

                    //TODO depends on 48 or 24kHz sampling
                    uint16_t bitrate;
                    if(bitrateIdx <= M1L2_BITRATE_IDX_MAX) {
                        bitrate = M1L2_BITRATE_IDX[bitrateIdx];
                    } else {
                        break;
                    }

                    uint8_t samplingIdx = static_cast<uint8_t>((*frameIter & 0x0C) >> 2);
                    uint16_t samplingRate;

                    if(samplingIdx <= M1_SAMPLINGFREQUENCY_IDX_MAX) {
                        samplingRate = M1_SAMPLINGFREQUENCY_IDX[samplingIdx];
                        if(mpegAudioVersionId == 0) {
                            samplingRate /= 2;
                        }
                    } else {
                        break;
                    }

                    bool paddingFlag = ((*frameIter & 0x02) >> 1) != 0;
                    bool privateFlag = (*frameIter++ & 0x01) != 0;

                    uint8_t channelMode = static_cast<uint8_t>((*frameIter & 0xC0) >> 6);
                    uint8_t channels;
                    if(channelMode <= CHANNELMODE_IDX_MAX) {
                        channels = CHANNELMODE_IDX[channelMode];
                    } else {
                        break;
                    }

                    //std::cout << m_logTag << " ID: " << +mpegAudioVersionId << " SamplingIdx: " << +samplingIdx << " SampleRate: " << samplingRate << " Channels: " << +channels << std::endl;

                    uint8_t modeExtension = static_cast<uint8_t>((*frameIter & 0x30) >> 4);
                    bool copyRight = ((*frameIter & 0x08) >> 3) != 0;
                    bool original = ((*frameIter & 0x04) >> 2) != 0;
                    uint8_t emphasis = static_cast<uint8_t>(*frameIter++ & 0x03);

                    uint8_t scaleCrcLen;
                    if (channelMode == 0x03) {
                        if(bitrateIdx > 0x02 ) {
                            scaleCrcLen = 4;
                        } else {
                            scaleCrcLen = 2;
                        }
                    } else {
                        if (bitrateIdx > 0x06) {
                            scaleCrcLen = 4;
                        } else {
                            scaleCrcLen = 2;
                        }
                    }

                    auto padRiter = frameData.rbegin();
                    while (padRiter < frameData.rend()) {
                        //F-PAD
                        bool ciPresent = ((*padRiter & 0x02) >> 1) != 0;
                        bool z = (*padRiter & 0x01) != 0;
                        //if(z) {
                        //    std::cout << m_logTag << " Z-Field: " << std::boolalpha << z << std::noboolalpha << std::endl;
                        //}
                        ++padRiter; //Byte L data field

                        uint8_t fPadType = static_cast<uint8_t>((*padRiter & 0xC0) >> 6); //Byte L-1 data field
                        uint8_t xPadIndicator = static_cast<uint8_t>((*padRiter++ & 0x30) >> 4);

                        if(fPadType == 0) {
                            padRiter += scaleCrcLen;
                            auto scaleRiterPos = padRiter;

                            switch (xPadIndicator) {
                                case 0: {
                                    //m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false);
                                    break;
                                }
                                case 1: {
                                    std::vector<uint8_t> padData;
                                    //padData.insert(padData.end(), frameData.end()-1-2-scaleCrcLen-4, frameData.end()-1-2-scaleCrcLen);
                                    padData.insert(padData.end(), frameData.end()-2-scaleCrcLen-4, frameData.end()-2-scaleCrcLen);
                                    //padData.insert(padData.end(), frameData.end()-1-2, frameData.end());
                                    padData.insert(padData.end(), frameData.end()-2, frameData.end());

                                    //std::string dl(padData.begin(), padData.end());
                                    //std::cout << m_logTag << "Short PAD: " << dl << std::endl;
                                    //m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false);
                                    m_padDataDispatcher.invoke(padData);

                                    break;
                                }
                                case 2: {
                                    //std::vector<uint8_t> audioData;
                                    std::vector<uint8_t> padData;

                                    if(ciPresent) {
                                        m_noCiLastLength = 0;
                                        for(int i = 0; i < 4; i++) {
                                            uint8_t xpadLengthIndex = static_cast<uint8_t>((*padRiter & 0xE0) >> 5);
                                            uint8_t xpadAppType = static_cast<uint8_t>(*padRiter++ & 0x1f);

                                            ++m_noCiLastLength;
                                            if(xpadAppType == 0) {
                                                break;
                                            }

                                            m_noCiLastLength += XPAD_SIZE[xpadLengthIndex];
                                        }

                                        padRiter += (m_noCiLastLength-1);

                                        padData.insert(padData.end(), frameData.end()-1-2-scaleCrcLen-m_noCiLastLength, frameData.end()-2-scaleCrcLen);
                                        padData.insert(padData.end(), frameData.end()-2, frameData.end());

                                        //audioData.insert(audioData.end(), frameData.begin(), frameData.end()-1-2-scaleCrcLen-m_noCiLastLength);
                                        //m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false);
                                        m_padDataDispatcher.invoke(padData);
                                    } else {
                                        if(m_noCiLastLength > 0) {
                                            padData.insert(padData.end(), frameData.end()-2-scaleCrcLen-m_noCiLastLength, frameData.end()-2-scaleCrcLen);
                                            padData.insert(padData.end(), frameData.end()-2, frameData.end());

                                            //audioData.insert(audioData.end(), frameData.begin(), frameData.end()-1-2-scaleCrcLen-m_noCiLastLength);
                                            //m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false);

                                            m_padDataDispatcher.invoke(padData);
                                        }
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }

                            m_audioDataDispatcher.invoke(frameData, 0, channels, samplingRate, false, false);
                        } else {
                            std::cout << m_logTag << " Wrong FPAD Type: " << +fPadType << std::endl;
                        }

                        break;
                    }

                    break;
                } else {
                    std::cout << m_logTag << "Wrong MPEG Syncword" << std::endl;
                    break;
                }
            }
        }
    }
}
#endif

void DabMpegServiceComponentDecoder::clearCallbacks() {
    DabServiceComponentDecoder::clearCallbacks();
    //std::cout << m_logTag << " clearCallbacks" << std::endl;
    m_padDataDispatcher.clear();
    m_audioDataDispatcher.clear();

}
