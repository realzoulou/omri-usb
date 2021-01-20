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

#include "paddecoder.h"

#include <iostream>

#include "global_definitions.h"

constexpr uint8_t PadDecoder::XPAD_SIZE[];

PadDecoder::PadDecoder() {

}

PadDecoder::~PadDecoder() {

}

void PadDecoder::padDataInput(const std::vector<uint8_t>& padData) {
    auto padRiter = padData.rbegin();
    while (padRiter < padData.rend()) {
        //F-PAD
        const bool ciPresent = (((*padRiter & 0x02u) >> 1u) & 0x01u) != 0;
        const bool z = (*padRiter & 0x01u) != 0;
        if (z) {
            // Z: this bit shall be set to "0" for synchronization purposes in serial communication links
            std::cout << m_logTag << " Z-Field: " << std::boolalpha << z << std::noboolalpha
                      << std::endl;
        }
        padRiter++; //Byte L data field

        const auto fPadType = static_cast<uint8_t>(((*padRiter & 0xC0u) >> 6u) & 0x03u); //Byte L-1 data field

        /* Note on ETSI EN 300 401 V1.4.1 (2006-06):
         * Only F-PAD type "00" was carried over to V2.1.1.
         * F-PAD type "01" containing DRC (Dynamic Range Control), Origin (UPC/EAN) or ISRC data is silently ignored
         * F-PAD type "10" and "11" were already RFU in 1.4.1
         */

        const X_PAD_INDICATOR xPadIndicator = static_cast<X_PAD_INDICATOR>(((*padRiter++ & 0x30u) >> 4u) & 0x03u);
        //std::cout << m_logTag << " FPadType: " << +fPadType << " XPadInd: " << +xPadIndicator << " CI: " << std::boolalpha << ciPresent << " Z: " << z << std::noboolalpha << std::endl;

        std::vector<std::pair<uint8_t, PAD_APPLICATION_TYPE>> apps;

        if (fPadType == 0) {
            if (xPadIndicator == X_PAD_INDICATOR::NO_XPAD) {
                //NO Xpad
            }
                //short xpad
            else if (xPadIndicator == X_PAD_INDICATOR::SHORT_XPAD) {
                std::vector<uint8_t> xpadData;
                int xpadSize = 4;
                uint8_t xpadAppType = 0;
                if(ciPresent) {
                    //uint8_t rfu = (*padRiter & 0xE0) >> 5;
                    m_noCiLastShortXpAdAppType = xpadAppType = static_cast<uint8_t>(*padRiter++ & 0x1fu);
                    xpadSize = 3;
                } else {
                    xpadAppType = static_cast<uint8_t>(m_noCiLastShortXpAdAppType + 1);
                }

                apps.emplace_back(xpadSize, static_cast<PAD_APPLICATION_TYPE>(xpadAppType));
            } else if (xPadIndicator == X_PAD_INDICATOR::VARIABLE_XPAD) {
                //variable xpad

                if(ciPresent) {
                    for(int i = 0; i < 4; i++) {
                        auto xpadLengthIndex = static_cast<uint8_t>(((*padRiter & 0xE0u) >> 5u) & 0x07u);
                        auto xpadAppType = static_cast<uint8_t>(*padRiter++ & 0x1fu);
#if 0
                        if( (xpadAppType >= 4 && xpadAppType <= 11) || (xpadAppType >= 16 && xpadAppType <= 31) ) {
                            //std::cout << m_logTag << "[" << +i << "] XPadLength: " << +xpadLengthIndex << " : " << +XPAD_SIZE[xpadLengthIndex] << " AppType: " << +xpadAppType << std::endl;
                            auto uAppIter = m_userApps.find(xpadAppType);
                            if (uAppIter != m_userApps.cend()) {
                                std::cout << m_logTag << " Found Userapp for XPADApptype: "
                                          << +xpadAppType << " : " << +uAppIter->second->getXpadAppType()
                                          << " UAppType: " << +uAppIter->second->getUserApplicationType()
                                          << " DGUsed: " << std::boolalpha
                                          << uAppIter->second->dataGroupsUsed() << std::noboolalpha
                                          << " DSCTy: " << uAppIter->second->getDataServiceComponentType()
                                          << std::endl;
                            }
                        }
#endif
                        if(xpadAppType == static_cast<uint8_t>(PAD_APPLICATION_TYPE::END_MARKER)) {
                            break;
                        }

                        m_noCiLastLength = PadDecoder::XPAD_SIZE[xpadLengthIndex];
                        m_noCiLastXpAdAppType = xpadAppType;

                        apps.emplace_back(PadDecoder::XPAD_SIZE[xpadLengthIndex], static_cast<PAD_APPLICATION_TYPE>(xpadAppType));
                        //std::cout << m_logTag << "X-PAD CI [" << +i << "]: AppTy=" << +xpadAppType << ", len=" << +(PadDecoder::XPAD_SIZE[xpadLengthIndex]) << std::endl;
                    }
                } else {
                    if(m_noCiLastLength > 0 || m_noCiLastXpAdAppType != 0xFF) {
                        //if the last xpadapptype was not a continuation
                        if(!(m_noCiLastXpAdAppType%2)) {
                            ++m_noCiLastXpAdAppType;
                            //std::cout << m_logTag << "X-PAD No CI: last AppTy=" << +m_noCiLastXpAdAppType << std::endl;
                        }
                        apps.emplace_back(padData.size()-2, static_cast<PAD_APPLICATION_TYPE>(m_noCiLastXpAdAppType));
                        //std::cout << m_logTag << "X-PAD No CI: AppTy=" << +m_noCiLastXpAdAppType << ", len=" << +(padData.size()-2) << std::endl;
                    }
                }
            }

            for(const std::pair<uint8_t, PAD_APPLICATION_TYPE>& app : apps) {
                std::vector<uint8_t> xpadDataSubfield(padRiter, padRiter+app.first);
                padRiter += app.first;

                if(app.second == PAD_APPLICATION_TYPE::DATA_GROUP_LENGTH_INDICATOR) {
                    m_currentDataGroup.clear();
                    if(CRC_CCITT_CHECK(xpadDataSubfield.data(), static_cast<uint8_t>(xpadDataSubfield.size()))) {
                        m_currentDataGroupLength = static_cast<uint16_t>((xpadDataSubfield[0] & 0x3Fu) << 8u | (xpadDataSubfield[1] & 0xFFu));
                        //std::cout<< m_logTag << "DATA_GROUP_LENGTH " << +m_currentDataGroupLength << std::endl;
                    } else {
                        std::cout<< m_logTag << "DATA_GROUP_LENGTH_INDICATOR CRC failed" << std::endl;
                        m_currentDataGroupLength = 0;
                    }
                }

                if ((app.second == PAD_APPLICATION_TYPE::MOT_DATAGROUP_START ||
                            app.second == PAD_APPLICATION_TYPE::MOT_DATAGROUP_CONTINUATION)
                        && (m_currentDataGroupLength > 0)) { // Note: START or CONTINUATION may come in, but no LENGTH_IND seen yet, avoid useless processing

                    auto dataSize = xpadDataSubfield.size();
                    /** ETSI EN 300 401, chap. 7.4.2.2 (V2.1.1 and V1.4.1 are the same)
                     * If the final part of an X-PAD data group does not entirely fill the X-PAD data sub-field in which it is transported,
                     * padding bits shall be used to fill the X-PAD sub-field. Padding bits shall be set to zero
                     */

                    if (app.second == PAD_APPLICATION_TYPE::MOT_DATAGROUP_CONTINUATION) {
                        auto remainingData = m_currentDataGroupLength - m_currentDataGroup.size();
                        if(dataSize <= remainingData) {
                            m_currentDataGroup.insert(m_currentDataGroup.end(), xpadDataSubfield.begin(), xpadDataSubfield.end());

                        } else {
                            m_currentDataGroup.insert(m_currentDataGroup.end(), xpadDataSubfield.begin(), xpadDataSubfield.begin() + remainingData);
                        }
                        //std::cout << m_logTag << "MOT_DATAGROUP_CONTINUATION " << +m_currentDataGroup.size() << "/" << +m_currentDataGroupLength << " bytes" << std::endl;
                    }
                    else if(app.second == PAD_APPLICATION_TYPE::MOT_DATAGROUP_START) {
                        if (dataSize <= m_currentDataGroupLength) {
                            //std::cout << m_logTag << "MOT_DATAGROUP_START with " << +xpadDataSubfield.size() << std::endl;
                            m_currentDataGroup.insert(m_currentDataGroup.end(), xpadDataSubfield.begin(),xpadDataSubfield.end());
                        } else {
                            //std::cout << m_logTag << "MOT_DATAGROUP_START with " << +xpadDataSubfield.size() << " bytes (only " << +m_currentDataGroupLength << " used)" << std::endl;
                            m_currentDataGroup.insert(m_currentDataGroup.end(), xpadDataSubfield.begin(),xpadDataSubfield.begin() + m_currentDataGroupLength);
                        }
                    }
                    // all MOT data group sub-fields collected?
                    if (m_currentDataGroupLength == m_currentDataGroup.size()) {
                        m_motDecoder.motDataInput(m_currentDataGroup);
                    }
                }

                if(app.second == PAD_APPLICATION_TYPE::DLS_DATAGROUP_START || app.second == PAD_APPLICATION_TYPE::DLS_DATAGROUP_CONTINUATION) {
                    auto uappIter = m_userAppDecoders.find(registeredtables::USERAPPLICATIONTYPE::DYNAMIC_LABEL);
                    if(uappIter != m_userAppDecoders.cend()) {
                        uappIter->second->applicationDataInput(xpadDataSubfield, app.second);
                    }
                }
            }
        } else {
            // F-PAD types from old ETSI EN 300 401 V1.4.1 (2006-06) are silently ignored
            break;
        }
    }
}

void PadDecoder::addUserApplication(std::shared_ptr<DabUserApplication> uApp) {
    //std::cout << m_logTag << " +++++++++++ Adding UserApp: " << +uApp->getUserApplicationType() << std::endl;
    m_userApps.insert(std::make_pair(uApp->getXpadAppType(), uApp));

}

void PadDecoder::addUserApplicationDecoder(std::shared_ptr<DabUserapplicationDecoder> uAppDecoder) {
    if(uAppDecoder != nullptr) {
        //std::cout << m_logTag << " +++++++++++ Adding UserAppDecoder: " << +uAppDecoder->getUserApplicationType() << std::endl;
        m_userAppDecoders.insert(
                std::make_pair(uAppDecoder->getUserApplicationType(), uAppDecoder));
        if (uAppDecoder->getUserApplicationType() ==
            registeredtables::USERAPPLICATIONTYPE::MOT_SLIDESHOW ||
            uAppDecoder->getUserApplicationType() ==
            registeredtables::USERAPPLICATIONTYPE::MOT_BROADCAST_WEBSITE ||
            uAppDecoder->getUserApplicationType() == registeredtables::USERAPPLICATIONTYPE::EPG) {
            //std::cout << m_logTag << " +++++++++++ Adding MotUserAppDecoder: " << +uAppDecoder->getUserApplicationType() << std::endl;
            m_motDecoder.addUserapplicationDecoder(
                    std::static_pointer_cast<DabMotUserapplicationDecoder>(uAppDecoder));
        }
    } else {
        std::cout << m_logTag << " +++++++++++ UserAppDecoder is null!" << std::endl;
    }
}

void PadDecoder::reset() {
    auto uAppIter = m_userAppDecoders.cbegin();
    while(uAppIter != m_userAppDecoders.cend()) {
        uAppIter->second->reset();
        ++uAppIter;
    }
}
