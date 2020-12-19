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

#include <iomanip>

#include <pthread.h>
#include <unistd.h>
#include <cerrno>

constexpr uint8_t DabMpegServiceComponentDecoder::XPAD_SIZE[8];
constexpr uint16_t DabMpegServiceComponentDecoder::M1L2_BITRATE_IDX[];
constexpr uint16_t DabMpegServiceComponentDecoder::M1_SAMPLINGFREQUENCY_IDX[];
constexpr uint8_t DabMpegServiceComponentDecoder::CHANNELMODE_IDX[];

DabMpegServiceComponentDecoder::DabMpegServiceComponentDecoder() {
    std::cout << m_logTag << "Creating" << std::endl;

    m_processThreadRunning = true;
    m_processThread = std::thread(&DabMpegServiceComponentDecoder::processData, this);
}

DabMpegServiceComponentDecoder::~DabMpegServiceComponentDecoder() {
    std::cout << m_logTag << "Destroying" << std::endl;

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
    //std::cout << m_logTag << "componentDataInput: " << frameData.size() << " - " << +m_frameSize << " : " << +m_subChanBitrate << " : " << std::hex << std::setfill('0') << std::setw(2) << +frameData[0] << +frameData[1] << +frameData[2] << std::dec << std::endl;

    if(synchronized) {
        m_conQueue.push(frameData);
    } else {
        if (m_frameSize > 0) {
            synchronizeData(frameData);
        }
    }
}

void DabMpegServiceComponentDecoder::synchronizeData(const std::vector<uint8_t>& unsyncData) {
    std::vector<uint8_t> data;
    if(!m_unsyncDataBuffer.empty()) {
        // prepend any left over data from previous call
        data.insert(data.begin(), m_unsyncDataBuffer.begin(), m_unsyncDataBuffer.end());
        m_unsyncDataBuffer.clear();
    }

    data.insert(data.end(), unsyncData.begin(), unsyncData.end());

    auto unsyncIter = data.begin();
    auto frameStartIter = data.begin();
    auto frameEndIter = data.begin();

    while(unsyncIter < data.end()) {
        if (*unsyncIter == 0xFF && ((*(unsyncIter+1)) & 0xF0u) == 0xF0) {
            frameStartIter = unsyncIter;
            unsyncIter += 2; // don't test the same 2 bytes again
            frameEndIter = unsyncIter;
            while (unsyncIter < data.end()) {
                // test for MPEG sync word, but ignore occasional 0xFFF if frame is not yet long enough
                auto frameLen = std::distance(frameStartIter, unsyncIter);
                if (*unsyncIter == 0xFF && ((*(unsyncIter+1)) & 0xF0u) == 0xF0 && frameLen >= m_frameSize ) {
                    m_conQueue.push(std::vector<uint8_t>(frameStartIter, frameEndIter));
                    frameStartIter = unsyncIter;
                }
                ++unsyncIter; ++frameEndIter;
            }
        } else {
            ++unsyncIter;
        }
    }
    auto remainLen = std::distance(frameStartIter, data.end());
    if (remainLen > 0) {
        // store data to be processed in next call
        m_unsyncDataBuffer.insert(m_unsyncDataBuffer.begin(), frameStartIter, data.end());
    }
}

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
                if(*frameIter++ == 0xFF && (*frameIter & 0xF0) == 0xF0) {
                    // https://en.wikipedia.org/wiki/MPEG_elementary_stream#General_layout_of_MPEG-1_audio_elementary_stream
                    const auto mpegAudioVersionId = static_cast<uint8_t>(((*frameIter & 0x08) >> 3) & 0x01);
                    const auto mpegAudioLayer = static_cast<uint8_t>(((*frameIter & 0x06) >> 1) & 0x03);
                    const bool crcProtection = (*frameIter++ & 0x01) != 0;

                    const auto bitrateIdx = static_cast<uint8_t>(((*frameIter & 0xF0) >> 4) & 0x0F);

                    uint16_t bitrate;
                    if(bitrateIdx <= M1L2_BITRATE_IDX_MAX) {
                        bitrate = M1L2_BITRATE_IDX[bitrateIdx];
                    } else {
                        std::clog << m_logTag << "bitrateIdx not supported:" << +bitrateIdx << std::endl;
                        break; // skip this whole frame
                    }

                    const auto samplingIdx = static_cast<uint8_t>(((*frameIter & 0x0C) >> 2) & 0x03);
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

                    const bool paddingFlag = (((*frameIter & 0x02) >> 1) & 0x01) != 0;
                    const bool privateFlag = (*frameIter++ & 0x01) != 0;

                    const auto channelMode = static_cast<uint8_t>(((*frameIter & 0xC0) >> 6) & 0x03);
                    auto channels = CHANNELMODE_IDX[0];
                    if(channelMode <= CHANNELMODE_IDX_MAX) {
                        channels = CHANNELMODE_IDX[channelMode];
                    } else {
                        std::cout << m_logTag << "channelMode not supported:" << +channelMode << std::endl;
                        break; // skip this whole frame
                    }

                    //std::cout << m_logTag << " ID: " << +mpegAudioVersionId << " SamplingIdx: " << +samplingIdx << " SampleRate: " << samplingRate << " Channels: " << +channels << std::endl;

                    const auto modeExtension = static_cast<uint8_t>(((*frameIter & 0x30) >> 4) & 0x03);
                    const bool copyRight = (((*frameIter & 0x08) >> 3) & 0x01) != 0;
                    const bool original = (((*frameIter & 0x04) >> 2) & 0x01) != 0;
                    const auto emphasis = static_cast<uint8_t>(*frameIter++ & 0x03);

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
                        const bool ciPresent = (((*padRiter & 0x02) >> 1) & 0x01) != 0;
                        const bool z = (*padRiter & 0x01) != 0;

                        ++padRiter; //Byte L data field
                        const auto fPadType = static_cast<uint8_t>(((*padRiter & 0xC0) >> 6) & 0x03); //Byte L-1 data field
                        xPadIndicator = static_cast<PadDecoder::X_PAD_INDICATOR>(((*padRiter++ & 0x30) >> 4) & 0x03);

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
                                                const auto xpadLengthIndex = static_cast<uint8_t>((*padRiter & 0xE0) >> 5);
                                                const auto xpadAppType = static_cast<uint8_t>(*padRiter++ & 0x1f);

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

void DabMpegServiceComponentDecoder::clearCallbacks() {
    DabServiceComponentDecoder::clearCallbacks();
    //std::cout << m_logTag << " clearCallbacks" << std::endl;
    m_padDataDispatcher.clear();
    m_audioDataDispatcher.clear();

}
