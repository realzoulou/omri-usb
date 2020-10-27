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

#ifndef PADDECODER_H
#define PADDECODER_H

#include <vector>
#include <memory>

#include <map>

#include "dabuserapplication.h"
#include "motdecoder.h"

class PadDecoder {

public:
    explicit PadDecoder();
    virtual ~PadDecoder();

    void padDataInput(const std::vector<uint8_t>& padData);

    void addUserApplication(std::shared_ptr<DabUserApplication> uApp);
    void addUserApplicationDecoder(std::shared_ptr<DabUserapplicationDecoder> uAppDecoder);

    void reset();

private:
    const std::string m_logTag{"[PadDecoder]"};

    std::map<uint8_t, std::shared_ptr<DabUserApplication>> m_userApps;
    std::map<registeredtables::USERAPPLICATIONTYPE, std::shared_ptr<DabUserapplicationDecoder>> m_userAppDecoders;

    uint16_t m_currentDataGroupLength{0};
    std::vector<uint8_t> m_currentDataGroup;

    uint8_t m_noCiLastLength{0};
    uint8_t m_noCiLastXpAdAppType{0xFF};
    uint8_t m_noCiLastShortXpAdAppType{0xFF};

    MotDecoder m_motDecoder;

private:
    static constexpr uint8_t XPAD_SIZE[8] {
        4, 6, 8, 12, 16, 24, 32, 48
    };

public:
    static const uint32_t SHORT_XPAD_LEN = 4;
    static const uint32_t FPAD_LEN = 2;
    static const uint32_t VAR_XPAD_MAX_CI = 4;

    enum X_PAD_INDICATOR {
        NO_XPAD = 0,
        SHORT_XPAD = 1,
        VARIABLE_XPAD = 2,
        RFU = 3,
        INVALID = 255
    };

    struct MOT_HEADERMODE {
        uint8_t continuityIdx;
        uint16_t segmentNumber;
        uint16_t transportId;
        std::vector<uint8_t> motData;
    };
};

#endif // PADDECODER_H
