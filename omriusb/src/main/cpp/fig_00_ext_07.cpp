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

#include "fig_00_ext_07.h"

#include <iostream>

Fig_00_Ext_07::Fig_00_Ext_07(const std::vector<uint8_t> &figData) : Fig_00(figData) {
    parseFigData(figData);
}

Fig_00_Ext_07::~Fig_00_Ext_07() {

}

void Fig_00_Ext_07::parseFigData(const std::vector<uint8_t> &figData) {
    auto figIter = figData.cbegin() +1;
    while(figIter < figData.cend()) {
        uint8_t services = (*figIter & 0xFC) >> 2;
        uint16_t count = (*figIter++ & 0x03) << 8 | (*figIter++ & 0xFF);

        //std::cout << m_logTag << " NumServices: " << +services << " Count: " << +count << std::endl;

        /** ETSI EN 300 401 V2.1.1, 6.4.2 Configuration information
         * A receiver can use the Services field to determine if the complete MCI has been received.
         * This is especially useful under weak signal conditions when FIBs with erroneous CRCs are detected.
         *
         * A receiver can use the Count field to verify if the configuration of an already known ensemble remains unchanged
         * - if the received Count matches the stored Count then the configuration is the same.
         * This can be used to speed up use cases like background scanning.
         * The rest of the MCI need not be collected to judge if the stored configuration is still valid.
         *
         * TODO: Use this for a super fast start of a service
         *
         * NOTE: FIG 0/7 is *not* present in 300 401 V1.4.1
         */
    }
}
