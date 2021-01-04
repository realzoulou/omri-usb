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

#ifndef DABPLUSSERVICECOMPONENTDECODER_H
#define DABPLUSSERVICECOMPONENTDECODER_H

#include <vector>
#include <string>
#include <thread>
#include <atomic>

#include <iostream>

#include "dabservicecomponentdecoder.h"
#include "concurrent_queue.h"

#include "registered_tables.h"
#include "global_definitions.h"

// uncomment to use original implementation of Fabian
//#define USE_ORIG_SYNC_N_PROCESSDATA

class DabPlusServiceComponentDecoder : public DabServiceComponentDecoder {

public:
    DabPlusServiceComponentDecoder();
    virtual ~DabPlusServiceComponentDecoder();

    virtual void setSubchannelBitrate(uint16_t bitrate) override;
    virtual void componentDataInput(const std::vector<uint8_t>& frameData, bool synchronized) override;
    virtual void flushBufferedData() override;

    //special case for MscStreamAudio
    using PAD_DATA_CALLBACK = std::function<void (const std::vector<uint8_t>&)>;
    virtual std::shared_ptr<PAD_DATA_CALLBACK> registerPadDataCallback(PAD_DATA_CALLBACK cb);

    using AUDIO_COMPONENT_DATA_CALLBACK = std::function<void(const std::vector<uint8_t>&, int, int, int, bool, bool)>;
    virtual std::shared_ptr<AUDIO_COMPONENT_DATA_CALLBACK> registerAudioDataCallback(AUDIO_COMPONENT_DATA_CALLBACK cb);

    virtual void clearCallbacks() override;

private:
    struct DabSuperFrame {
        std::vector<uint8_t> superFrameData;
        uint8_t numAUs{0};
        std::vector<uint16_t> auLengths;
        std::vector<uint16_t> auStarts;
        bool sbrUsed{false};
        bool psUsed{false};
        int samplingRate{-1};
        int channels{-1};
        void clear() {superFrameData.clear(); numAUs = 0; auLengths.clear(); auStarts.clear(); sbrUsed = false; psUsed = false; samplingRate = -1; channels = -1;}
    };

private:
    class RSDecoder {
    private:
        void *rs_handle;
        uint8_t rs_packet[120];
        int corr_pos[10];
    public:
        RSDecoder();
        ~RSDecoder();

        bool DecodeSuperFrame(uint8_t *sf, size_t sf_len);
    };

    RSDecoder m_rsDec;

private:
    const std::string m_logTag = "[DabPlusServiceComponentDecoder]";

    static const uint8_t NUM_FRAMES_PER_SUPER_FRAME = 5;
    static const uint8_t NUM_BYTES_FOR_FIRE_CODE_CHECK = 11;

    uint16_t m_superFrameSize{0};

    static bool combineSuperFrame(/*out*/ DabSuperFrame& superFrame,
            /*in*/ std::vector<std::vector<uint8_t>> & frameRingBuffer,
            /*in*/ uint8_t frameRingBufNextIdx);

    bool decodeSuperFrameHeader(const std::vector<uint8_t> & superFrameData,
                                       const uint16_t superFrameSize, /* != superFrameData.size() !! */
                                       bool & dacRate,
                                       bool & sbr,
                                       bool & chanMode,
                                       bool & ps,
                                       uint8_t & numAUs,
                                       std::vector<uint16_t> & auStarts,
                                       std::vector<uint16_t> & auLengths,
                                       bool verbose) const ;

    ConcurrentQueue <std::vector<uint8_t>> m_conQueue;
    CallbackDispatcher<std::function<void (const std::vector<uint8_t>&)>> m_padDataDispatcher;
    CallbackDispatcher<AUDIO_COMPONENT_DATA_CALLBACK> m_audioDataDispatcher;

    std::atomic<bool> m_processThreadRunning{false};
    std::thread m_processThread;

    // mutex to guard read/write access to internal data
    // use it by placing following code as (typ.) first instruction of a method:
    //   std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    // by leaving the method, the mutex is automatically released
    std::recursive_mutex m_mutex;

    void resetInSync();
    std::vector<uint8_t> m_unsyncDataBuffer;
    bool m_unsyncSync{false};
    int m_unsyncFrameCount{0};
    unsigned m_unsyncByteCount{0};

private:
    void processData();
    void synchronizeData(const std::vector<uint8_t>& unsyncData);

#ifdef USE_ORIG_SYNC_N_PROCESSDATA
    void processDataOrig();
    void synchronizeDataOrig(const std::vector<uint8_t>& unsyncData);
    DabSuperFrame m_currentSuperFrame;
    bool m_isSync{false};
    int m_dabSuperFrameCount{0};
#endif // USE_ORIG_SYNC_N_PROCESSDATA

private:
    static const uint16_t FIRECODE_TABLE[256];

    static bool CHECK_FIRECODE(const uint8_t* const frameData, size_t size);
};

#endif // DABPLUSSERVICECOMPONENTDECODER_H
