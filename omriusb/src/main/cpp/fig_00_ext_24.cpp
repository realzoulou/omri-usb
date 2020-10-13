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

#include <iostream>
#include <iomanip>
#include <sstream>

#include "fig_00_ext_24.h"
// helper template and method for dumping a vector of uint8_t as hex to a string stream
template <typename T> std::string to_hex(T data) {
    std::ostringstream result;
    result << std::setw(2) << std::setfill('0') << std::hex << std::uppercase << static_cast<int>(data);
    return result.str();
}

static std::string toHexString(const std::vector<uint8_t>& data) {
    if (data.empty()) return "";
    auto size = data.size();
    std::ostringstream result;
    if (size > 0) {
        result << "0x";
    }
    for(size_t i=0; i < size; i++)
    {
        result << to_hex(data[i]);
        if (i != size)
            result << " ";
    }
    return result.str();
}

Fig_00_Ext_24::Fig_00_Ext_24(const std::vector<uint8_t> &figData) : Fig_00(figData) {
    std::cout << m_logTag << toHexString(figData) << std::endl;
    parseFigData(figData);
}

Fig_00_Ext_24::~Fig_00_Ext_24() {

}

void Fig_00_Ext_24::parseFigData(const std::vector<uint8_t> &figData) {
    auto figIter = figData.cbegin() +1;
    while(figIter < figData.cend()) {
        OtherEnsembleServiceInformation oeInfo;
        oeInfo.isOtherEnsemble = isOtherEnsemble();
        if(!isDataService()) {
            oeInfo.serviceId = static_cast<uint32_t>(((*figIter++ & 0xFF) << 8) | (*figIter++ & 0xFF));
        } else {
            oeInfo.serviceId = static_cast<uint32_t>(((*figIter++ & 0xFF) << 24) | ((*figIter++ & 0xFF) << 16) | ((*figIter++ & 0xFF) << 8) | (*figIter++ & 0xFF));
        }

        //bool rfa = ((*figIter & 0x80) >> 7) & 0xFF;
        oeInfo.caId = static_cast<uint8_t>((((*figIter & 0x70) >> 4) & 0xFF));
        uint8_t numEids = static_cast<uint8_t>(((*figIter++ & 0x0F) & 0xFF));
        if(numEids == 0) {
            oeInfo.isChangeEvent = true;
        }

        //std::cout << m_logTag << " SId: 0x" << std::hex << +oeInfo.serviceId << " is in Ensemble with EId: ";
        for(uint8_t i = 0; i < numEids; i++) {
            uint16_t eid = static_cast<uint16_t>(((*figIter++ & 0xFF) << 8) | (*figIter++ & 0xFF));
            oeInfo.ensembleIds.push_back(eid);
            //std::cout << std::hex << "0x" << +eid << std::dec <<  ", ";
        }

        /* spec says "The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the SId field."
         * my two cents: This makes no sense. The SId may be 32 bits if P/D = 1
         * Thus: we use only SId as the database key
         */
        oeInfo.oeDbKey = /* static_cast<uint32_t>(isOtherEnsemble() << 31 | isDataService() << 30 | */ oeInfo.serviceId /*)*/;

        m_oeSrvInfos.push_back(oeInfo);
        //std::cout << " OtherEnsemble: " << std::boolalpha << isOtherEnsemble() << std::noboolalpha << " ServiceInformationVersion_SIV: " << std::boolalpha << isNextConfiguration() << std::noboolalpha << std::endl;
    }
}
