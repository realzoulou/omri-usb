/*
 * Copyright (C) 2020 realzoulou
 *
 * Author:
 *  realzoulou
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

#ifndef LINKEDSERVICEDAB_H
#define LINKEDSERVICEDAB_H

#include <cstdint>

class LinkedServiceDab {

public:
    explicit LinkedServiceDab() = default;
    explicit LinkedServiceDab(uint8_t ecc, uint32_t sid, uint32_t eid, uint32_t efreqKHz,
                              bool isProgrammeService) :
        m_ecc(ecc), m_sid(sid), m_eid(eid), m_efreq(efreqKHz), m_isProgrammeService(isProgrammeService) {}

    virtual ~LinkedServiceDab() = default;

    void setEnsembleEcc(uint8_t ecc) { m_ecc = ecc; }
    uint8_t getEnsembleEcc() const { return m_ecc; }

    void setEnsembleId(uint16_t eid) { m_eid = eid; }
    uint16_t getEnsembleId() const { return m_eid; }

    void setEnsembleFrequencyKHz(uint32_t freq) { m_efreq = freq; }
    uint32_t getEnsembleFrequencyKHz() const { return m_efreq; }

    void setServiceId(uint32_t sid) { m_sid = sid; }
    uint32_t getServiceId() const { return m_sid; }

    void setIsProgrammeService(bool isPS) { m_isProgrammeService = isPS; }
    bool getIsProgrammeService() const { return m_isProgrammeService; }

    inline bool operator==(const LinkedServiceDab & other) const {
        return other.m_ecc == m_ecc && other.m_eid == m_eid && other.m_sid == m_sid
            && other.m_efreq == m_efreq && other.m_isProgrammeService == m_isProgrammeService;
    }
    inline bool operator!=(const LinkedServiceDab & other) const { return !operator==(other); }

    inline std::string to_string() const {
        std::stringstream str;
        str << "ecc 0x" << std::hex << +m_ecc << ",sid 0x" << +m_sid << ",eid 0x" << +m_eid
            << std::dec << "," << +m_efreq << " kHz, ps " << std::boolalpha << m_isProgrammeService
            << std::noboolalpha;
        return str.str();
    }

private:
    uint8_t m_ecc{0};
    uint32_t m_sid{0};
    uint16_t m_eid{0};
    uint32_t m_efreq{0}; // kHz
    bool m_isProgrammeService{false};
};

#endif // LINKEDSERVICEDAB_H
