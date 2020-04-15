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
#include "fig_00_ext_17.h"

Fig_00_Ext_17::Fig_00_Ext_17(const std::vector<uint8_t>& figData) : Fig_00(figData) {
    parseFigData(figData);
}

Fig_00_Ext_17::~Fig_00_Ext_17() {

}

void Fig_00_Ext_17::parseFigData(const std::vector<uint8_t>& figData) {
    auto figIter = figData.cbegin() +1;
    while(figIter < figData.cend()) {
        ProgrammeTypeInformation ptyInfo;

        auto remainingBytes = std::distance(figIter, figData.cend());
        if (remainingBytes == 4) {
            // assume ETSI EN 300 401 v2.1.1
            ptyInfo.serviceId = static_cast<uint16_t>(((*figIter++ & 0xFF) << 8) |
                                                      (*figIter++ & 0xFF));
            ptyInfo.isDynamic = (((*figIter & 0x80) >> 7) & 0x01) != 0;
            //bool rfa1 = ((*figIter & 0x40) >> 6) & 0xFF;
            //uint8_t rfu1 = ((*figIter & 0x30) >> 4) & 0xFF;
            uint8_t rfa2 = static_cast<uint8_t>(((*figIter++ & 0x0F) << 2) |
                                                (((*figIter & 0xC0) >> 6) & 0xFF));
            //bool rfu2 = ((*figIter & 0x20) >> 5) & 0xFF;
            ptyInfo.intPtyCode = static_cast<uint8_t>(*figIter++ & 0x1F);

            m_ptyInformations.push_back(ptyInfo);

        } else {
            // assume ETSI EN 300 401 v1.4.1
            ptyInfo.serviceId = static_cast<uint16_t>(((*figIter++ & 0xFF) << 8) |
                                                      (*figIter++ & 0xFF));
            ptyInfo.isDynamic = (((*figIter & 0x80) >> 7) & 0x01) != 0;
            //bool ps = (((*figIter & 0x40) >> 6) & 0x01) != 0;
            bool Lflag = (((*figIter & 0x20) >> 5) & 0x01) != 0; // language field present (1) or absent (0)
            bool CCflag = (((*figIter & 0x10) >> 4) & 0x01) != 0;
            uint8_t rfa = static_cast<uint8_t>(*figIter++ & 0x0F);
            if (Lflag) {
                uint8_t lang = static_cast<uint8_t>(*figIter++);
            }
            //uint8_t rfa2 = static_cast<uint8_t>(((*figIter & 0xC0) >> 6) & 0x03);
            //bool rfu = (((*figIter & 0x20) >> 5) & 0x01) != 0;
            ptyInfo.intPtyCode = static_cast<uint8_t>((((*figIter & 0x10) << 5) |
                                                    (*figIter++ & 0x0F)) & 0x1F);
            if (CCflag) {
                //uint8_t rfa3 = static_cast<uint8_t>(((*figIter & 0xC0) >> 6) & 0x03);
                //bool rfu2 = (((*figIter & 0x20) >> 5) & 0x01) != 0;
                uint8_t compcode = static_cast<uint8_t>((((*figIter & 0x10) << 5) |
                                                         (*figIter++ & 0x0F)) & 0x1F);
            }
            m_ptyInformations.push_back(ptyInfo);

        }
    }
}

const std::vector<Fig_00_Ext_17::ProgrammeTypeInformation>& Fig_00_Ext_17::getProgrammeTypeInformations() const {
    return m_ptyInformations;
}
