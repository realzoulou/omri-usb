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
#include <sstream>
#include <iomanip>

#include "ficparser.h"

constexpr uint16_t FicParser::CRC_CCITT_TABLE[];

bool FicParser::FIB_CRC_CHECK(const uint8_t* data) const {

    //fixed fib size
    uint16_t dataLen = FIB_SIZE;

    //initial register
    uint16_t crc = 0xffff;
    uint16_t crc2 = 0xffff;

    uint16_t crcVal, i;
    uint8_t  crcCalData;

    for (i = 0; i < (dataLen - 2); i++) {
        crcCalData = *(data+i);
        crc = (crc << 8)^FicParser::CRC_CCITT_TABLE[(crc >> 8)^(crcCalData)++];
    }

    crcVal = *(data+i) << 8u;
    crcVal = crcVal | *(data+i+1);

    crc2 = (crcVal^crc2);

    return crc == crc2;
}

//calling inherited constructor with FIC_ID
FicParser::FicParser() {
    std::cout << M_LOG_TAG << " Constructing" << std::endl;

    m_fibProcessorThread = std::thread(&FicParser::processFib, this);
}

FicParser::~FicParser() {
    std::cout << M_LOG_TAG << " Destructing" << std::endl;
    m_fibDataQueue.clear();
    m_fibProcessThreadRunning = false;

    if(m_fibProcessorThread.joinable()) {
        std::cout << M_LOG_TAG << " Joining FIB processor thread " << getParserThreadName() << std::endl;
        m_fibProcessorThread.join();
    }
}

void FicParser::call(const std::vector<uint8_t> &data) {
    auto ficIter = data.cbegin();
    int loop = 0;
    while (ficIter < data.cend()) {
        loop++;
        long remainingBytes = std::distance(ficIter, data.cend());
        if (remainingBytes < FIB_SIZE) {
            std::clog << M_LOG_TAG << "FIB " << +loop << " too short: exp:" << FIB_SIZE << ", rcv:" << +remainingBytes << std::endl;
        }
        std::vector<uint8_t> fib(ficIter, ficIter+FIB_SIZE);
        if(FIB_CRC_CHECK(fib.data())) {
            m_fibDataQueue.push(fib);
        } else {
            std::clog << M_LOG_TAG << "FIB " << +loop << " crc corrupted: " << Fig::toHexString(fib) << std::endl;
        }

        ficIter += FIB_SIZE;
    }
}

void FicParser::processFib() {
    m_fibProcessThreadRunning = true;

    pid_t self = pthread_self();
    char name[13]; // 4 + 8 + '\0'
    snprintf(name, 12, "FIB-%08x", (int) self);
    name[12] = '\0';
    pthread_setname_np(pthread_self(), name); // crashes when passing self instead of pthread_self()
    m_ficProcessorThreadName = name;

    std::cout << M_LOG_TAG << "FIB Processor thread started: " << m_ficProcessorThreadName << std::endl;

    while (m_fibProcessThreadRunning) {
        std::vector<uint8_t> fibData;
        if(m_fibDataQueue.tryPop(fibData, std::chrono::milliseconds(24))) {
            try {
                auto figIter = fibData.begin();
                auto remainingBytes = std::distance(figIter, fibData.end());
                if (remainingBytes < 2) {
                    std::cout << M_LOG_TAG << "popped FIB too short: exp:2, rcv:" << +remainingBytes
                              << std::endl;
                }
                while (figIter < fibData.end() - 2 && m_fibProcessThreadRunning) {
                    uint8_t figType = static_cast<uint8_t>((*figIter & 0xE0) >> 5);
                    uint8_t figLength = static_cast<uint8_t>(*figIter & 0x1F);

                    ++figIter;

                    if (figType != 7 && figLength != 31 && figLength > 0) {
                        remainingBytes = std::distance(figIter, fibData.end());
                        if (remainingBytes < figLength) {
                            std::cout << M_LOG_TAG << "FIG too short: exp:" << +figLength
                                      << ", rcv:"
                                      << +remainingBytes << std::endl;
                        }
                        const std::vector<uint8_t> figData(figIter, figIter + figLength);
                        switch (figType) {
                            case 0: {
                                parseFig_00(figData);
                                break;
                            }
                            case 1: {
                                parseFig_01(figData);
                                break;
                            }
                            default:
                                std::cout << M_LOG_TAG << "Unknown FIG Type: " << +figType
                                          << std::endl;
                                break;
                        }

                        figIter += figLength;
                    } else {
                        // figType=7, figLength=31: End Marker
                        // -OR-
                        // figLength = 0
                        break;
                    }
                }
            } catch (std::exception& e) {
                std::clog << M_LOG_TAG << "Caught exception: " << e.what() << std::endl;
                std::clog << M_LOG_TAG << "FIB size: " << +fibData.size() << std::endl;
                std::clog << M_LOG_TAG << Fig::toHexString(fibData) << std::endl;
            }
        }
    }
    std::cout << M_LOG_TAG << "FIB Processor thread stopped: " << name << std::endl;
    m_ficProcessorThreadName = "";
    m_fibProcessThreadRunning = false;
}

void FicParser::parseFig_00(const std::vector<uint8_t>& ficData) {
    //ficData[0] ensured by the calling thread - figLEngth > 0
    switch (ficData[0] & 0x1Fu) {
        case Fig::FIG_00_TYPE::ENSEMBLE_INFORMATION: {
            Fig_00_Ext_00 extZero(ficData);
            m_fig00_00dispatcher.invoke(extZero);
            m_fig00DoneDispatcher.invoke(Fig::FIG_00_TYPE::ENSEMBLE_INFORMATION);
            break;
        }
        case Fig::FIG_00_TYPE::BASIC_SUBCHANNEL_ORGANIZATION: {
            Fig_00_Ext_01 extOne(ficData);
            m_fig00_01dispatcher.invoke(extOne);
            m_fig00DoneDispatcher.invoke(Fig::FIG_00_TYPE::BASIC_SUBCHANNEL_ORGANIZATION);
            break;
        }
        case Fig::FIG_00_TYPE::BASIC_SERVICE_COMPONENT_DEFINITION: {
            Fig_00_Ext_02 extTwo(ficData);
            m_fig00_02dispatcher.invoke(extTwo);
            m_fig00DoneDispatcher.invoke(Fig::FIG_00_TYPE::BASIC_SERVICE_COMPONENT_DEFINITION);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_PACKET_MODE: {
            Fig_00_Ext_03 extThree(ficData);
            m_fig00_03dispatcher.invoke(extThree);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_STREAM_CA: {
            Fig_00_Ext_04 extFour(ficData);
            m_fig00_04dispatcher.invoke(extFour);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_LANGUAGE: {
            Fig_00_Ext_05 extFive(ficData);
            m_fig00_05dispatcher.invoke(extFive);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_LINKING_INFORMATION: {
            Fig_00_Ext_06 extSix(ficData);
            m_fig00_06dispatcher.invoke(extSix);
            break;
        }
        case Fig::FIG_00_TYPE::CONFIGURATION_INFORMATION: {
            Fig_00_Ext_07 extSeven(ficData);
            m_fig00_07dispatcher.invoke(extSeven);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_GLOBAL_DEFINITION: {
            Fig_00_Ext_08 extEight(ficData);
            m_fig00_08dispatcher.invoke(extEight);
            m_fig00DoneDispatcher.invoke(Fig::FIG_00_TYPE::SERVICE_COMPONENT_GLOBAL_DEFINITION);
            break;
        }
        case Fig::FIG_00_TYPE::COUNTRY_LTO_INTERNATIONAL_TABLE: {
            Fig_00_Ext_09 extNine(ficData);
            m_fig00_09dispatcher.invoke(extNine);
            break;
        }
        case Fig::FIG_00_TYPE::DATE_AND_TIME: {
            Fig_00_Ext_10 extTen(ficData);
            m_fig00_10dispatcher.invoke(extTen);
            break;
        }
        case Fig::FIG_00_TYPE::USERAPPLICATION_INFORMATION: {
            Fig_00_Ext_13 ext3Ten(ficData);
            m_fig00_13dispatcher.invoke(ext3Ten);
            m_fig00DoneDispatcher.invoke(Fig::FIG_00_TYPE::USERAPPLICATION_INFORMATION);
            break;
        }
        case Fig::FIG_00_TYPE::FEC_SUBCHANNEL_ORGANIZATION: {
            Fig_00_Ext_14 ext4Ten(ficData);
            m_fig00_14dispatcher.invoke(ext4Ten);
            break;
        }
        case Fig::FIG_00_TYPE::PROGRAMME_NUMBER: {
            // is not needed and no more contained in v2.1.1
            break;
        }
        case Fig::FIG_00_TYPE::PROGRAMME_TYPE: {
            Fig_00_Ext_17 ext7Ten(ficData);
            m_fig00_17dispatcher.invoke(ext7Ten);
            break;
        }
        case Fig::FIG_00_TYPE::ANNOUNCEMENT_SUPPORT: {
            Fig_00_Ext_18 ext8Ten(ficData);
            m_fig00_18dispatcher.invoke(ext8Ten);
            break;
        }
        case Fig::FIG_00_TYPE::ANNOUNCEMENT_SWITCHING: {
            Fig_00_Ext_19 ext9Ten(ficData);
            m_fig00_19dispatcher.invoke(ext9Ten);
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_INFORMATION: {
            Fig_00_Ext_20 extTwenty(ficData);
            m_fig00_20dispatcher.invoke(extTwenty);
            break;
        }
        case Fig::FIG_00_TYPE::FREQUENCY_INFORMATION: {
            Fig_00_Ext_21 ext1Twenty(ficData);
            m_fig00_21dispatcher.invoke(ext1Twenty);
            break;
        }
        case Fig::FIG_00_TYPE::OE_SERVICES: {
            Fig_00_Ext_24 ext4Twenty(ficData);
            m_fig00_24dispatcher.invoke(ext4Twenty);
            break;
        }
        case Fig::FIG_00_TYPE::OE_ANNOUNCEMENT_SUPPORT: {
            Fig_00_Ext_25 ext5Twenty(ficData);
            m_fig00_25dispatcher.invoke(ext5Twenty);
            break;
        }
        case Fig::FIG_00_TYPE::OE_ANNOUNCEMENT_SWITCHING: {
            Fig_00_Ext_26 ext6Twenty(ficData);
            m_fig00_26dispatcher.invoke(ext6Twenty);
            break;
        }
        default:
            std::clog << M_LOG_TAG << "Unknown FIG 0/" << +(ficData[0] & 0x1Fu) << std::endl;
            break;
    }
}

void FicParser::parseFig_01(const std::vector<uint8_t>& ficData) {
    //ficData[0] ensured by the calling thread - figLEngth > 0
    switch(ficData[0] & 0x07u) {
        case Fig::FIG_01_TYPE::ENSEMBLE_LABEL: {
            Fig_01_Ext_00 extZero(ficData);
            m_fig01_00dispatcher.invoke(extZero);
            m_fig01DoneDispatcher.invoke(Fig::FIG_01_TYPE::ENSEMBLE_LABEL);
            break;
        }
        case Fig::FIG_01_TYPE::PROGRAMME_SERVICE_LABEL: {
            Fig_01_Ext_01 extOne(ficData);
            m_fig01_01dispatcher.invoke(extOne);
            m_fig01DoneDispatcher.invoke(Fig::FIG_01_TYPE::PROGRAMME_SERVICE_LABEL);
            break;
        }
        case Fig::FIG_01_TYPE::SERVICE_COMPONENT_LABEL: {
            Fig_01_Ext_04 ext4(ficData);
            m_fig01_04dispatcher.invoke(ext4);
            break;
        }
        case Fig::FIG_01_TYPE::DATA_SERVICE_LABEL: {
            Fig_01_Ext_05 ext5(ficData);
            m_fig01_05dispatcher.invoke(ext5);
            break;
        }
        case Fig::FIG_01_TYPE::XPAD_USERAPPLICATION_LABEL: {
            Fig_01_Ext_06 ext6(ficData);
            m_fig01_06dispatcher.invoke(ext6);
            m_fig01DoneDispatcher.invoke(Fig::FIG_01_TYPE::XPAD_USERAPPLICATION_LABEL);
            break;
        }
        default:
            std::clog << M_LOG_TAG << "Unknown Extension1: " << +(ficData[0] & 0x07u) << std::endl;
            break;
    }

}

std::shared_ptr<FicParser::Fig_00_Done_Callback> FicParser::registerFig_00_Done_Callback(Fig_00_Done_Callback cb) {
    return m_fig00DoneDispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_Done_Callback> FicParser::registerFig_01_Done_Callback(FicParser::Fig_01_Done_Callback cb) {
    return m_fig01DoneDispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_00_Callback> FicParser::registerFig_00_00_Callback(Fig_00_00_Callback cb) {
    return m_fig00_00dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_01_Callback> FicParser::registerFig_00_01_Callback(Fig_00_01_Callback cb) {
    return m_fig00_01dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_02_Callback> FicParser::registerFig_00_02_Callback(Fig_00_02_Callback cb) {
    return m_fig00_02dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_03_Callback> FicParser::registerFig_00_03_Callback(Fig_00_03_Callback cb) {
    return m_fig00_03dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_04_Callback> FicParser::registerFig_00_04_Callback(Fig_00_04_Callback cb) {
    return m_fig00_04dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_05_Callback> FicParser::registerFig_00_05_Callback(Fig_00_05_Callback cb) {
    return m_fig00_05dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_06_Callback> FicParser::registerFig_00_06_Callback(Fig_00_06_Callback cb) {
    return m_fig00_06dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_07_Callback> FicParser::registerFig_00_07_Callback(Fig_00_07_Callback cb) {
    return m_fig00_07dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_08_Callback> FicParser::registerFig_00_08_Callback(Fig_00_08_Callback cb) {
    return m_fig00_08dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_09_Callback> FicParser::registerFig_00_09_Callback(Fig_00_09_Callback cb) {
    return m_fig00_09dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_10_Callback> FicParser::registerFig_00_10_Callback(Fig_00_10_Callback cb) {
    return m_fig00_10dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_13_Callback> FicParser::registerFig_00_13_Callback(Fig_00_13_Callback cb) {
    return m_fig00_13dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_14_Callback> FicParser::registerFig_00_14_Callback(Fig_00_14_Callback cb) {
    return m_fig00_14dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_17_Callback> FicParser::registerFig_00_17_Callback(Fig_00_17_Callback cb) {
    return m_fig00_17dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_18_Callback> FicParser::registerFig_00_18_Callback(Fig_00_18_Callback cb) {
    return m_fig00_18dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_19_Callback> FicParser::registerFig_00_19_Callback(Fig_00_19_Callback cb) {
    return m_fig00_19dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_20_Callback> FicParser::registerFig_00_20_Callback(Fig_00_20_Callback cb) {
    return m_fig00_20dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_21_Callback> FicParser::registerFig_00_21_Callback(Fig_00_21_Callback cb) {
    return m_fig00_21dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_24_Callback> FicParser::registerFig_00_24_Callback(Fig_00_24_Callback cb) {
    return m_fig00_24dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_25_Callback> FicParser::registerFig_00_25_Callback(Fig_00_25_Callback cb) {
    return m_fig00_25dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_00_26_Callback> FicParser::registerFig_00_26_Callback(Fig_00_26_Callback cb) {
    return m_fig00_26dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_00_Callback> FicParser::registerFig_01_00_Callback(Fig_01_00_Callback cb) {
    return m_fig01_00dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_01_Callback> FicParser::registerFig_01_01_Callback(Fig_01_01_Callback cb) {
    return m_fig01_01dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_04_Callback> FicParser::registerFig_01_04_Callback(Fig_01_04_Callback cb) {
    return m_fig01_04dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_05_Callback> FicParser::registerFig_01_05_Callback(Fig_01_05_Callback cb) {
    return m_fig01_05dispatcher.add(cb);
}

std::shared_ptr<FicParser::Fig_01_06_Callback> FicParser::registerFig_01_06_Callback(Fig_01_06_Callback cb) {
    return m_fig01_06dispatcher.add(cb);
}

void FicParser::reset() {
    m_fibDataQueue.clear();
    ficBuffer.clear();
}
