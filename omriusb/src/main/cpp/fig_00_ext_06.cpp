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

#include <algorithm>
#include <iostream>
#include <iomanip>
//#include <sstream>

#include "fig_00_ext_06.h"

Fig_00_Ext_06::Fig_00_Ext_06(const std::vector<uint8_t> &figData) : Fig_00(figData) {
    //std::cout << m_logTag << Fig::toHexString(figData) << std::endl;
    parseFigData(figData);
}

Fig_00_Ext_06::~Fig_00_Ext_06() {

}

void Fig_00_Ext_06::parseFigData(const std::vector<uint8_t> &figData) {

    auto figIter = figData.cbegin() +1;
    while(figIter < figData.cend()) {

        ServiceLinkingInformation sli;
        sli.isDataService = isDataService();

        bool idListFlag = (((*figIter & 0x80) >> 7) & 0xFF) != 0;

        /* ETSI TS 103 176 V2.4.1, 5.2 Service linking
         *
         * The short form has the Id list flag set to 0.
         *  a. When the activation state of the linkage sets is being signalled the C/N flag is set to 1.
         *  b. When the content of the database associated with a particular linkage set will be changed
         *     - due to a network configuration change - the C/N flag is set to 0.
         *
         *  The long form has the Id list flag set to 1.
         *  The C/N flag is used in the database definition mode and the flag indicates
         *  a. the start of the database definition when set to 0 or
         *  b. a continuation of the database definition when set to 1.
         */

        bool isDbStart{false};
        if(idListFlag) {
            // long form
            sli.isContinuation = isNextConfiguration();
            isDbStart = !isNextConfiguration();
        } else {
            // short form = Database Change Event Indication (CEI)
            sli.isChangeEvent = true;
        }

        bool laFlag = (((*figIter & 0x40) >> 6) & 0xFF) != 0;
        sli.linkageActive = laFlag;

        bool shFlag = (((*figIter & 0x20) >> 5) & 0xFF) != 0;
        sli.isSoftLink = !shFlag;

        bool ilsFlag = (((*figIter & 0x10) >> 4) & 0xFF) != 0;
        sli.isIls = ilsFlag;

        uint16_t linkageSetNumber = static_cast<uint16_t>((*figIter++ & 0x0F) << 8 | (*figIter++ & 0xFF));
        sli.linkageSetNumber = linkageSetNumber;

        if(idListFlag) {
            ServiceLinkList sll;

            //bool rfu = ((*ficData & 0x80) >> 7) & 0xFF;
            auto lq = static_cast<uint8_t>(((*figIter & 0x60) >> 5) & 0xFF);
            auto idLq = static_cast<Fig_00_Ext_06::SERVICE_LINKING_IDENTIFIER_LIST_QUALIFIER>(lq);
            sll.idListQualifier = idLq;

            //bool rfa = static_cast<bool>(((*figIter & 0x10) >> 4) & 0xFF);
            uint8_t numIds = static_cast<uint8_t>((*figIter++ & 0x0F) & 0xFF);
            for (uint8_t i = 0; i < numIds; i++) {
                uint32_t id{0x00};

                if(!isDataService() && !ilsFlag) {
                    id = static_cast<uint16_t>((*figIter++ & 0xFF) << 8 | (*figIter++ & 0xFF));
                }

                if (!isDataService() && ilsFlag) {
                    uint8_t ecc = static_cast<uint8_t>((*figIter++ & 0xFF));
                    uint16_t sid = static_cast<uint16_t>((*figIter++ & 0xFF) << 8 | (*figIter++ & 0xFF));

                    id = ((ecc << 16) | sid);
                }

                if(isDataService()){
                    id = static_cast<uint32_t>((*figIter++ & 0xFF) << 24 | (*figIter++ & 0xFF) << 16 |  (*figIter++ & 0xFF) << 8 | (*figIter++ & 0xFF));
                }

                if(i == 0 && isDbStart) {
                    //std::cout << m_logTag << " KeyServiceId: 0x" << std::hex << +id << std::dec << std::endl;
                    sli.keyServiceId = id;
                } else {
                    //std::cout << m_logTag << " LinkId: 0x" << std::hex << +id << std::dec << std::endl;
                    sll.idList.push_back(id);
                }
            }
            sli.serviceLinks.push_back(sll);
        }

        sli.linkDbKey = static_cast<uint16_t>(isOtherEnsemble() << 15 | isDataService() << 14 | shFlag << 13 | ilsFlag << 12 | (linkageSetNumber & 0x0FFF));

        m_sli.push_back(sli);
    }
}
