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

#include "dabensemble.h"

#include <functional>
#include <iostream>
#include <sstream>

#include "timer.h"

DabEnsemble::DabEnsemble() {
    std::cout << m_logTag << " Constructing" << std::endl;
    m_ficPtr = std::unique_ptr<FicParser>(new FicParser);
}

DabEnsemble::~DabEnsemble() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " Destructing" << std::endl;
    if (m_ficPtr != nullptr) {
        m_ficPtr.reset();
    }
    std::cout << m_logTag << " Destructed " << std::endl;
}

void DabEnsemble::reset() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " Resetting Ensemble" << std::endl;

    m_resetting = true;
    m_ensembleCollectFinished = false;

    if (m_ficPtr != nullptr) {
        m_ficPtr.get()->stop();
        m_ficPtr.get()->reset();
        registerCbs();
        m_ficPtr.get()->start();
    }

    m_ensembleId = EID_INVALID;
    m_cifCntHigh = 0x00;
    m_cifCntLow = 0x00;
    m_cifCntHighNext = 0x00;
    m_cifCntLowNext = 0x00;
    m_announcementsSupported = false;
    m_ensembleEcc = ECC_INVALID;

    m_labelCharset = CHARSET_INVALID;
    m_ensembleLabel.clear();
    m_ensembleShortLabel.clear();

    m_streamComponentsMap.clear();
    m_packetComponentsMap.clear();
    m_servicesMap.clear();

    m_resetting = false;
}

uint16_t DabEnsemble::getEnsembleId() const {
    return m_ensembleId;
}

uint8_t DabEnsemble::getCurrentCifCountHigh() const {
    return m_cifCntHigh;
}

uint8_t DabEnsemble::getCurrentCifCountLow() const {
    return m_cifCntLow;
}

bool DabEnsemble::isAlarmAnnouncementSupported() const {
    return m_announcementsSupported;
}

uint8_t DabEnsemble::getEnsembleLabelCharset() const {
    return m_labelCharset;
}

std::string DabEnsemble::getEnsembleLabel() const {
    return m_ensembleLabel;
}

std::string DabEnsemble::getEnsembleShortLabel() const {
    return m_ensembleShortLabel;
}

uint8_t DabEnsemble::getEnsembleEcc() const {
    return m_ensembleEcc;
}

std::vector<std::shared_ptr<DabService>> DabEnsemble::getDabServices() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::vector<std::shared_ptr<DabService>> services;
    for(const auto& srv : m_servicesMap) {
        services.push_back(std::make_shared<DabService>(srv.second));
    }
    return services;
}

void DabEnsemble::registerCbs() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    m_fig000done = false;
    m_fig001done = false;
    m_fig002done = false;
    m_fig003done = false;
    m_fig008done = false;
    m_fig013done = false;
    m_fig014done = false;
    m_fig017done = false;
    m_fig0done = false;

    m_fig100done = false;
    m_fig101done = false;
    m_fig104done = false;
    m_fig105done = false;
    m_fig106done = false;
    m_fig1done = false;

    m_00DonePtr = m_ficPtr.get()->registerFig_00_Done_Callback(std::bind(&DabEnsemble::fig_00_done_cb, this, std::placeholders::_1));
    m_01DonePtr = m_ficPtr.get()->registerFig_01_Done_Callback(std::bind(&DabEnsemble::fig_01_done_cb, this, std::placeholders::_1));

    m_00Ptr = m_ficPtr.get()->registerFig_00_00_Callback(std::bind(&DabEnsemble::fig00_00_input, this, std::placeholders::_1));
    m_01Ptr = m_ficPtr.get()->registerFig_00_01_Callback(std::bind(&DabEnsemble::fig00_01_input, this, std::placeholders::_1));
    m_02Ptr = m_ficPtr.get()->registerFig_00_02_Callback(std::bind(&DabEnsemble::fig00_02_input, this, std::placeholders::_1));
    m_03Ptr = m_ficPtr.get()->registerFig_00_03_Callback(std::bind(&DabEnsemble::fig00_03_input, this, std::placeholders::_1));
    m_08Ptr = m_ficPtr.get()->registerFig_00_08_Callback(std::bind(&DabEnsemble::fig00_08_input, this, std::placeholders::_1));
    m_09Ptr = m_ficPtr.get()->registerFig_00_09_Callback(std::bind(&DabEnsemble::fig00_09_input, this, std::placeholders::_1));
    m_010Ptr = m_ficPtr.get()->registerFig_00_10_Callback(std::bind(&DabEnsemble::fig00_10_input, this, std::placeholders::_1));

    m_013Ptr = m_ficPtr.get()->registerFig_00_13_Callback(std::bind(&DabEnsemble::fig00_13_input, this, std::placeholders::_1));
    m_014Ptr = m_ficPtr.get()->registerFig_00_14_Callback(std::bind(&DabEnsemble::fig00_14_input, this, std::placeholders::_1));
    m_017Ptr = m_ficPtr.get()->registerFig_00_17_Callback(std::bind(&DabEnsemble::fig00_17_input, this, std::placeholders::_1));

    //ServiceFollowing
    m_06Ptr = m_ficPtr.get()->registerFig_00_06_Callback(std::bind(&DabEnsemble::fig00_06_input, this, std::placeholders::_1));
    m_21Ptr = m_ficPtr.get()->registerFig_00_21_Callback(std::bind(&DabEnsemble::fig00_21_input, this, std::placeholders::_1));
    m_24Ptr = m_ficPtr.get()->registerFig_00_24_Callback(std::bind(&DabEnsemble::fig00_24_input, this, std::placeholders::_1));

    m_10Ptr = m_ficPtr.get()->registerFig_01_00_Callback(std::bind(&DabEnsemble::fig01_00_input, this, std::placeholders::_1));
    m_11Ptr = m_ficPtr.get()->registerFig_01_01_Callback(std::bind(&DabEnsemble::fig01_01_input, this, std::placeholders::_1));
    m_14Ptr = m_ficPtr.get()->registerFig_01_04_Callback(std::bind(&DabEnsemble::fig01_04_input, this, std::placeholders::_1));
    m_15Ptr = m_ficPtr.get()->registerFig_01_05_Callback(std::bind(&DabEnsemble::fig01_05_input, this, std::placeholders::_1));
    m_16Ptr = m_ficPtr.get()->registerFig_01_06_Callback(std::bind(&DabEnsemble::fig01_06_input, this, std::placeholders::_1));

    //Announcements
    m_18Ptr = m_ficPtr.get()->registerFig_00_18_Callback(std::bind(&DabEnsemble::fig00_18_input, this, std::placeholders::_1));
    m_19Ptr = m_ficPtr.get()->registerFig_00_19_Callback(std::bind(&DabEnsemble::fig00_19_input, this, std::placeholders::_1));
}

void DabEnsemble::unregisterCbsAfterEnsembleCollect() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);

    m_00DonePtr.reset(); m_00DonePtr = nullptr;
    m_00Ptr.reset(); m_00Ptr = nullptr;
    m_01Ptr.reset(); m_01Ptr = nullptr;
    m_02Ptr.reset(); m_02Ptr = nullptr;
    m_03Ptr.reset(); m_03Ptr = nullptr;
    m_09Ptr.reset(); m_09Ptr = nullptr;
    m_08Ptr.reset(); m_08Ptr = nullptr;
    m_013Ptr.reset(); m_013Ptr = nullptr;
    m_014Ptr.reset(); m_014Ptr = nullptr;
    m_017Ptr.reset();  m_017Ptr = nullptr;

    m_01DonePtr.reset(); m_01DonePtr = nullptr;
    m_10Ptr.reset(); m_10Ptr = nullptr;
    m_11Ptr.reset(); m_11Ptr = nullptr;
    m_14Ptr.reset(); m_14Ptr = nullptr;
    m_15Ptr.reset(); m_15Ptr = nullptr;
    m_16Ptr.reset(); m_16Ptr = nullptr;
}
void DabEnsemble::dataInput(const std::vector<uint8_t>& data, uint8_t subChId, bool synchronized) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(!m_resetting) {
        if(subChId != 0x64) {
            auto compIter = m_streamComponentsMap.find(subChId);
            if(compIter != m_streamComponentsMap.cend()) {
                compIter->second->componentMscDataInput(data, synchronized);
            } else {
                auto dataCompIter = m_packetComponentsMap.cbegin();
                while(dataCompIter != m_packetComponentsMap.cend()) {
                    if(dataCompIter->second->getSubChannelId() == subChId) {
                        dataCompIter->second->componentMscDataInput(data, synchronized);
                    }
                    ++dataCompIter;
                }
            }
        } else {
            m_ficPtr->call(data);
        }
    }
}

//added to flush component decoders
void DabEnsemble::flushBufferedComponentData(uint8_t subChId) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(!m_resetting) {
        auto compIter = m_streamComponentsMap.find(subChId);
        if(compIter != m_streamComponentsMap.cend()) {
            compIter->second->flushBufferedData();
        } else {
            auto dataCompIter = m_packetComponentsMap.cbegin();
            while(dataCompIter != m_packetComponentsMap.cend()) {
                if(dataCompIter->second->getSubChannelId() == subChId) {
                    dataCompIter->second->flushBufferedData();
                }
                ++dataCompIter;
            }
        }
    }
}

//added to flush component decoders
void DabEnsemble::flushAllBufferedComponentData() {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    auto audioCompIter = m_streamComponentsMap.cbegin();
    while(audioCompIter != m_streamComponentsMap.cend()) {
        audioCompIter->second->flushBufferedData();
        ++audioCompIter;
    }

    auto dataCompIter = m_packetComponentsMap.cbegin();
    while(dataCompIter != m_packetComponentsMap.cend()) {
        dataCompIter->second->flushBufferedData();
        ++dataCompIter;
    }
}

void DabEnsemble::fig00_00_input(const Fig_00_Ext_00& fig00) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(m_ensembleId != fig00.getEnsembleId()) {
        m_isInitializing = true;
    }

    m_ensembleId = fig00.getEnsembleId();
    m_announcementsSupported = fig00.isAlarmSupported();

    m_cifCntHigh = fig00.getCifCountHigh();
    m_cifCntLow = fig00.getCifCountLow();

    if((m_cifCntHigh != m_cifCntHighNext || m_cifCntLow != m_cifCntLowNext) && !m_isInitializing) {
        //std::cout << m_logTag << " CIF Counter interrupted" << std::endl;
        //std::cout << m_logTag << " FIG 00 Ext 00 CifCnt    : " << +m_cifCntHigh << ":" << +m_cifCntLow << std::endl;
        //std::cout << m_logTag << " FIG 00 Ext 00 CifCntNext: " << +m_cifCntHighNext << ":" << +m_cifCntLowNext << std::endl;
    }

    m_cifCntHighNext = static_cast<uint8_t>((m_cifCntLowNext >= 246) ? ((m_cifCntHigh+1) % 20) : m_cifCntHigh);
    m_cifCntLowNext = static_cast<uint8_t>((m_cifCntLow + 4) % 250);

    if(fig00.getChangeFlag() != Fig_00_Ext_00::NO_CHANGE) {
        //std::cout << m_logTag << " Fig_00_Ext_00 ChangeFlag: " << +fig00.getChangeFlag() << " OccChange: " << +fig00.getOccurenceChange() << std::endl;
    }
    if(fig00.isNextConfiguration()) {
        //std::cout << m_logTag << " Fig_00_Ext_00 nextConfiguration" << std::endl;
    }

    m_isInitializing = false;
}

void DabEnsemble::fig00_01_input(const Fig_00_Ext_01& fig01) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    for(const auto& subOrga : fig01.getSubchannelOrganizations()) {
        auto streamCompIter = m_streamComponentsMap.find(subOrga.subChannelId);
        if(streamCompIter != m_streamComponentsMap.cend()) {
            streamCompIter->second->setMscStartAddress(subOrga.startAddress);
            streamCompIter->second->setSubchannelSize(subOrga.subChannelSize);
            streamCompIter->second->setConvolutionalCodingRate(subOrga.convolutionalCodingRate);
            streamCompIter->second->setProtectionLevelString(fig01.PROTECTIONLEVEL_STRING[subOrga.protectionLevelType]);
            streamCompIter->second->setProtectionLevel(subOrga.protectionLevel);
            streamCompIter->second->setProtectionType(subOrga.protectionType);
            streamCompIter->second->setUepTableIndex(subOrga.uepTableIndex);
            streamCompIter->second->setSubchannelBitrate(subOrga.subChannelBitrate);
        } else {
            auto packetCompIter = m_packetComponentsMap.begin();
            while(packetCompIter != m_packetComponentsMap.end()) {
                if(packetCompIter->second->getSubChannelId() == subOrga.subChannelId) {
                    packetCompIter->second->setMscStartAddress(subOrga.startAddress);
                    packetCompIter->second->setSubchannelSize(subOrga.subChannelSize);
                    packetCompIter->second->setConvolutionalCodingRate(subOrga.convolutionalCodingRate);
                    packetCompIter->second->setProtectionLevelString(fig01.PROTECTIONLEVEL_STRING[subOrga.protectionLevelType]);
                    packetCompIter->second->setProtectionLevel(subOrga.protectionLevel);
                    packetCompIter->second->setProtectionType(subOrga.protectionType);
                    packetCompIter->second->setUepTableIndex(subOrga.uepTableIndex);
                    packetCompIter->second->setSubchannelBitrate(subOrga.subChannelBitrate);
                }

                packetCompIter++;
            }
        }
    }
}

void DabEnsemble::fig00_02_input(const Fig_00_Ext_02 &fig02) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    for(const auto& srvDesc : fig02.getServiceDescriptions()) {
        auto srvIter = m_servicesMap.find(srvDesc.serviceId);
        if(srvIter == m_servicesMap.end()) {

            DabService service;

            if(srvDesc.isProgrammeService) {
                service.setServiceId(srvDesc.serviceId & 0x0000FFFFu);
            } else {
                service.setServiceId(srvDesc.serviceId);
            }

            service.setIsProgrammeService(srvDesc.isProgrammeService);
            service.setCaId(srvDesc.caId);
            service.setNumberOfServiceComponents(srvDesc.numServiceComponents);

            for(const auto& srvCom : srvDesc.serviceComponents) {
                std::shared_ptr<DabServiceComponent> componentPtr;

                switch (srvCom.transportModeId) {
                    case Fig_00_Ext_02::TMID::MSC_STREAM_AUDIO:
                    case Fig_00_Ext_02::TMID::MSC_STREAM_DATA: {
                        auto compIter = m_streamComponentsMap.find(srvCom.subChannelId);
                        if(compIter == m_streamComponentsMap.cend()) {
                            if(srvCom.transportModeId == Fig_00_Ext_02::TMID::MSC_STREAM_AUDIO) {
                                componentPtr = std::make_shared<DabServiceComponentMscStreamAudio>();
                                auto audioComponentPtr = std::static_pointer_cast<DabServiceComponentMscStreamAudio>(componentPtr);
                                audioComponentPtr->setAudioServiceComponentType(srvCom.serviceComponentType);
                            } else if(srvCom.transportModeId == Fig_00_Ext_02::TMID::MSC_STREAM_DATA) {
                                componentPtr = std::make_shared<DabServiceComponentMscStreamData>();
                                auto dataComponentPtr = std::static_pointer_cast<DabServiceComponentMscStreamData>(componentPtr);
                                dataComponentPtr->setDataServiceComponentType(srvCom.serviceComponentType);
                            }

                            componentPtr->setIsCaApplied(srvCom.isCaApplied);
                            componentPtr->setIsPrimary(srvCom.isPrimary);
                            componentPtr->setSubchannelId(srvCom.subChannelId);

                            m_streamComponentsMap.insert(std::make_pair(srvCom.subChannelId, componentPtr));

                            service.addServiceComponent(componentPtr);
                        } else {
                            service.addServiceComponent(compIter->second);
                        }

                        break;
                    }
                    case Fig_00_Ext_02::TMID::MSC_PACKET_MODE_DATA: {
                        auto compIter = m_packetComponentsMap.find(srvCom.serviceComponentId);
                        if(compIter == m_packetComponentsMap.cend()) {
                            auto packetComponentPtr = std::make_shared<DabServiceComponentMscPacketData>();
                            packetComponentPtr->setIsCaApplied(srvCom.isCaApplied);
                            packetComponentPtr->setIsPrimary(srvCom.isPrimary);
                            packetComponentPtr->setDataServiceComponentId(srvCom.serviceComponentId);

                            m_packetComponentsMap.insert(std::make_pair(srvCom.serviceComponentId, packetComponentPtr));
                            service.addServiceComponent(packetComponentPtr);
                        } else {
                            service.addServiceComponent(compIter->second);
                        }

                        break;
                    }
                default:
                    break;
                }
            }

            service.setEnsembleFrequency(m_ensembleFrequency);
            service.setDabEnsemble(this);
            m_servicesMap.emplace(srvDesc.serviceId, std::move(service));
        }
    }
}

void DabEnsemble::fig00_03_input(const Fig_00_Ext_03& fig03) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(m_packetComponentsMap.empty()) {
        return;
    }

    for(const auto& packetDesc : fig03.getPacketModeServiceDescriptions()) {
        auto compIter = m_packetComponentsMap.find(packetDesc.serviceComponentId);
        if(compIter != m_packetComponentsMap.cend()) {
            std::cout << m_logTag << " PacketComponent: " << std::hex << packetDesc.serviceComponentId << " setting SubchannelId: " << +packetDesc.subchannelId << std::dec << " PacketAddress: " << +packetDesc.packetAddress << " updating" << std::endl;
            compIter->second->setSubchannelId(packetDesc.subchannelId);
            compIter->second->setDataServiceComponentType(packetDesc.dataServiceComponentType);
            compIter->second->setCaOrganization(packetDesc.caOrganization);
            compIter->second->setIsDataGroupTransportUsed(packetDesc.dataGroupTransportUsed);
            compIter->second->setPacketAddress(packetDesc.packetAddress);
        }
    }
}

void DabEnsemble::fig00_06_input(const Fig_00_Ext_06& fig06) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //std::cout << m_logTag << " LinkDB Received SLI" << std::endl;
    bool hasChanged = false;
    for(const auto& linkInfo : fig06.getServiceLinkingInformations()) {
        auto linkDbIter = m_serviceLinkDb.find(linkInfo.linkDbKey);
        if(linkDbIter != m_serviceLinkDb.cend()) {
            //Linkageset already in Map
            if(!linkInfo.isChangeEvent) {
                if(linkInfo.isContinuation) {
                    for(const auto& srvLink : linkInfo.serviceLinks) {
                        if(!(*linkDbIter).second.containsServiceLinkList(srvLink)) {
                            (*linkDbIter).second.serviceLinks.push_back(srvLink);
                            hasChanged = true;
                            /*std::cout << m_logTag << " LinkDB update " << (linkInfo.isSoftLink ? "SoftLink " : "HardLink ") << "LinkageSetNumber: 0x" << std::hex << +linkInfo.linkageSetNumber << " with KeyServiceID: 0x" << +(*linkDbIter).second.keyServiceId << std::dec <<
                                " links to " << "\n\t";
                                for(const auto& sll : (*linkDbIter).second.serviceLinks) {
                                    std::cout << +sll.idList.size() << (sll.idListQualifier == 0 ? " DAB ServiceIds: " : " FM RDS PIs: ") << "\n\t\t";
                                    for(const auto& sllSrvId : sll.idList) {
                                        std::cout << "0x" << std::hex << +sllSrvId << "\n\t\t";
                                    }
                                    std::cout << std::dec;
                                }
                                std::cout << std::endl;*/
                        }
                    }
                }
            } else {
                /*
                 * ETSI TS 103 176 V2.4.1    5.2.5.1 Reaction to CEI
                 * When the CEI is received [...] receivers are informed that the part
                 * of the linkage database corresponding to the database key - OE, P/D, S/H, ILS, LSN - is about to be changed or
                 * deleted. The database entry corresponding to the database key is deleted (if present) and the receiver makes whatever
                 * preparation is needed to begin to build a new database entry for the indicated database key.
                 *
                 * NOTE: Since network configurations change infrequently, the reception of CEI is likely to be unusual
                 */
                // Note: Other than what the spec says, stations send this apparently regularily shortly after they added an entry.
                // Thus, let's violate the spec and ignore CEI
                /*std::clog << m_logTag << " LinkDB deleted LinkageSetNumber: 0x" << std::hex << +linkInfo.linkageSetNumber
                    << ", linkDbKey 0x" << +linkInfo.linkDbKey << std::dec << std::endl;
                m_serviceLinkDb.erase(linkDbIter);
                hasChanged = true;*/
            }
        } else {
            //Linkageset not yet in Map
            if(!linkInfo.isContinuation && !linkInfo.isChangeEvent) {
                //std::cout << m_logTag << " LinkDB adding new linkageSet with LinkDbKey: 0x" << std::hex << +linkInfo.linkDbKey << " and LinkageNumber: 0x" << +linkInfo.linkageSetNumber << " KeySeviceID: 0x" << +linkInfo.keyServiceId << std::dec << std::endl;
                m_serviceLinkDb.insert(std::make_pair(linkInfo.linkDbKey, linkInfo));
                hasChanged = true;
                std::cout << m_logTag << " LinkDB adding " << (linkInfo.isSoftLink ? "SoftLink" : "HardLink")
                    << " LinkageSetNumber: 0x" << std::hex << +linkInfo.linkageSetNumber << " KeyServiceID: 0x"
                    << +linkInfo.keyServiceId << " linkDbKey: 0x" << +linkInfo.linkDbKey << std::dec << std::endl;
            }
        }
    }
    if (hasChanged) {
        m_serviceFollowingDispatcher.invoke();
    }
}

void DabEnsemble::dumpServiceLinkDb() const {
    std::cout << m_logTag << "ServiceLinkDb entries: " << +m_serviceLinkDb.size() << std::endl;
    unsigned cnt = 0;
    for (auto const & iter : m_serviceLinkDb) {
        cnt++;
        std::cout << m_logTag << +cnt
                  << "|linkDbKey:0x" << std::hex << +iter.second.linkDbKey << std::dec
                  << ",soft:" << +iter.second.isSoftLink
                  << ",keyServiceId:0x" << std::hex << +iter.second.keyServiceId << std::dec
                  << ",setNr:0x" << std::hex << +iter.second.linkageSetNumber << std::dec
                  << ",isIls:" << +iter.second.isIls
                  << ",isData:" << +iter.second.isDataService
                  << ",links:" << +iter.second.serviceLinks.size() << std::endl;
        for (auto const & i : iter.second.serviceLinks) {
            std::string qualifier;
            switch (i.idListQualifier) {
                case Fig_00_Ext_06::DAB_SID:
                    qualifier = "DAB SId";
                    break;
                case Fig_00_Ext_06::RDS_PI:
                    qualifier = "RDS PI";
                    break;
                case Fig_00_Ext_06::DRM_AMSS_SID:
                    qualifier = "DRM_AMSS SId";
                    break;
                case Fig_00_Ext_06::RFU:
                    qualifier = "RFU";
                    break;
                default:
                    qualifier = "???";
                    break;
            }
            std::stringstream logString;
            logString << m_logTag << " |- " << qualifier << ":";
            for (auto j : i.idList) {
                logString << " 0x" << std::hex << +j << std::dec;
            }
            std::cout << logString.str() << std::endl;
        }
    }
}

void DabEnsemble::fig00_21_input(const Fig_00_Ext_21& fig21) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //std::cout << m_logTag << " FreqInfoDB received FrequencyInformation for OE: " << std::boolalpha << fig21.isOtherEnsemble() << std::noboolalpha << std::endl;
    bool hasChanged = false;
    for(const auto& freqInfo : fig21.getFrequencyInformations()) {
        auto freqInfoDbIter = m_frequencyInformationDb.find(freqInfo.freqDbKey);
        if(freqInfoDbIter != m_frequencyInformationDb.cend()) {
            // already in db
            if (!freqInfo.isChangeEvent) {
                if(freqInfo.isContinuation) {
                    if (std::find((*freqInfoDbIter).second.begin(), (*freqInfoDbIter).second.end(),
                                  freqInfo) == (*freqInfoDbIter).second.end()) {
                        //std::cout << m_logTag << " FreqInfoDB adding FreqInfo entry for ID: 0x" << std::hex << +freqInfo.id << std::dec << std::endl;
                        (*freqInfoDbIter).second.push_back(freqInfo);
                        hasChanged = true;
                    } else {
                        //std::cout << m_logTag << " FreqInfoDB already contains entry for ID: 0x" << std::hex << +freqInfo.id << std::dec << std::endl;
                    }
                }
            } else {
                /*
                 * ETSI TS 103 176 V2.4.1, 5.4.5.1 Reaction to CEI
                 * When the CEI is received [...] receivers are informed that the
                 * part of the Frequency information database corresponding to the database key - OE, P/D, Rfa, Id field and R&M field -
                 * is about to be changed or deleted. The database entry corresponding to the database key is deleted (if present) and the
                 * receiver makes whatever preparation is needed to begin to build a new database entry for the indicated database key.
                 *
                 * NOTE: Since network configurations change infrequently, the reception of CEI is likely to be unusual.
                 */
                std::cout << m_logTag << " FreqInfoDB deleted FreqInfo entry for ID: 0x" << std::hex << +freqInfo.id << std::dec << std::endl;
                m_frequencyInformationDb.erase(freqInfoDbIter);
                hasChanged = true;
            }
        } else {
            // not yet in map
            if(!freqInfo.isContinuation && !freqInfo.isChangeEvent) {
                //add new entry to db
                //std::cout << m_logTag << " FreqInfoDB adding new entry for ID: 0x" << std::hex << +freqInfo.id << std::dec << std::endl;
                m_frequencyInformationDb.insert(std::make_pair(freqInfo.freqDbKey,std::vector<Fig_00_Ext_21::FrequencyInformation>{freqInfo}));
                hasChanged = true;
            } else {
                /*std::cout << m_logTag << " FreqInfoDB not adding ID: 0x" << std::hex << +freqInfo.id << std::dec
                    << " isContinuation:" << std::boolalpha << freqInfo.isContinuation << ", isChangeEvent:" << freqInfo.isChangeEvent << std::noboolalpha
                    <<  std::endl;*/
            }
        }
    }
    if (hasChanged) {
        m_serviceFollowingDispatcher.invoke();
    }
}

void DabEnsemble::dumpFrequencyDb() const {
    std::cout << m_logTag << " FreqInfoDb entries: " << +m_frequencyInformationDb.size() << std::endl;
    unsigned cnt = 0;
    for (auto const & iter : m_frequencyInformationDb) {
        std::stringstream logString;
        unsigned cnt2 = 0;
        cnt++;
        std::string type;
        for (auto const & v : iter.second) {
            cnt2++;
            switch (v.frequencyInformationType) {
                case Fig_00_Ext_21::DAB_ENSEMBLE:
                    type = "DAB Ensemble";
                    break;
                case Fig_00_Ext_21::FM_RDS:
                    type = "FM RDS";
                    break;
                case Fig_00_Ext_21::AMSS:
                    type = "AMSS";
                    break;
                case Fig_00_Ext_21::DRM:
                    type = "DRM";
                    break;
                default:
                    type = "???";
                    break;
            }

            logString << m_logTag << " " << +cnt << ":" << +cnt2
                      << "|freqDbKey:0x" << std::hex << +v.freqDbKey << std::dec
                      << ",id:0x" << std::hex << +v.id << std::dec
                      << ",oe:" << std::boolalpha << v.isOtherEnsemble << std::noboolalpha
                      << ",type:" << type
                      << ",frequencies(" << +v.frequencies.size() << ")";
            for (auto const & freqInfo : v.frequencies) {
                std::string adjacent;
                if (v.frequencyInformationType == Fig_00_Ext_21::DAB_ENSEMBLE) {
                    switch (freqInfo.additionalInfo.dabEnsembleAdjacent) {
                        case Fig_00_Ext_21::DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED:
                            adjacent = "(geoAdjModeNotSignld)";
                            break;
                        case Fig_00_Ext_21::DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE:
                            adjacent = "(geoAdjModeOne)";
                            break;
                        case Fig_00_Ext_21::DabEnsembleAdjacent::GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED:
                            adjacent = "(NotAdjModeNotSignld)";
                            break;
                        case Fig_00_Ext_21::DabEnsembleAdjacent::GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_ONE:
                            adjacent = "(NotAdjModeOne)";
                            break;
                        case Fig_00_Ext_21::DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_UNKNOWN:
                            adjacent = "(geoAdjUnknown)";
                            break;
                        default:
                            adjacent = "???";
                            break;
                    }
                }
                logString << " " << +freqInfo.frequencyKHz << adjacent;
            }
            std::cout << logString.str() << std::endl;
        }
    }
}

void DabEnsemble::fig00_24_input(const Fig_00_Ext_24& fig24) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //std::cout << m_logTag << " OtherEnsembleSrvDb received OeServiceInformation" << std::endl;
    bool hasChanged = false;
    for(const auto& oeInfo : fig24.getOtherEnsembleServiceInformations()) {
        auto oeInfoDbIter = m_oeSrvInfoDb.find(oeInfo.oeDbKey);
        if (oeInfoDbIter != m_oeSrvInfoDb.cend()) {
            // already in db
            if (!oeInfo.isChangeEvent) {
                if (std::find((oeInfoDbIter)->second.cbegin(), (oeInfoDbIter)->second.cend(),
                        oeInfo) == (*oeInfoDbIter).second.cend()) {
                    //std::cout << m_logTag << " OeSrvInfoDb adding entry for ID: 0x" << std::hex << +oeInfo.serviceId << std::dec << std::endl;
                    (*oeInfoDbIter).second.push_back(oeInfo);
                    hasChanged = true;
                } else {
                    //std::cout << m_logTag << " OeSrvInfoDb already contains entry for ID: 0x" << std::hex << +oeInfo.serviceId << std::dec << std::endl;
                }
            } else {
                /*
                 * ETSI TS 103 176 V2.4.1, 5.3.5.1 Reaction to CEI
                 * When the CEI is received [...] receivers are informed that the
                 * part of the OE Services database corresponding to the database key - OE, P/D, SId - is about to be changed or deleted.
                 * The database entry corresponding to the database key is deleted (if present) and the receiver makes whatever
                 * preparation is needed to begin to build a new database entry in the database for the indicated database key.
                 *
                 * NOTE: Since network configurations change infrequently, the reception of CEI is likely to be unusual.
                 */
                std::cout << m_logTag << " OeSrvInfoDb deleting entry for ID: 0x" << std::hex << +oeInfo.serviceId << std::dec << std::endl;
                m_oeSrvInfoDb.erase(oeInfoDbIter);
                hasChanged = true;
            }
        } else {
            // not yet in map
            if (!oeInfo.isChangeEvent) {
                // add new entry to db
                //std::cout << m_logTag << " OeSrvInfoDb adding new entry for ID: 0x" << std::hex
                //          << +oeInfo.serviceId << std::dec << std::endl;
                m_oeSrvInfoDb.insert(std::make_pair(oeInfo.oeDbKey, std::vector<Fig_00_Ext_24::OtherEnsembleServiceInformation>{oeInfo}));
                hasChanged = true;
            }
        }
    }
    if (hasChanged) {
        m_serviceFollowingDispatcher.invoke();
    }
}

void DabEnsemble::dumpOeSrvInfoDb() const {
    std::cout << m_logTag << " OeSrvInfo entries: " << +m_frequencyInformationDb.size() << std::endl;
    unsigned cnt = 0;
    for (const auto & iter : m_oeSrvInfoDb) {
        std::stringstream logString;
        unsigned cnt2 = 0;
        for (auto const & v : iter.second) {
            cnt++;
            logString << m_logTag << " " << +cnt << ":" << +cnt2
                << "|oeDbKey:0x" << std::hex << +v.oeDbKey << std::dec
                << ",SId:0x" << std::hex << +v.serviceId << std::dec
                << ",oe:" << std::boolalpha << v.isOtherEnsemble << std::noboolalpha
                << ",caId:0x" << std::hex << +v.caId << std::dec
                << ",EIds(" << +v.ensembleIds.size() << ")";
            for (const auto & eid : v.ensembleIds) {
                logString << " 0x" << std::hex << +eid << std::dec;
            }
        }
        std::cout << logString.str() << std::endl;
    }
}

void DabEnsemble::fig00_08_input(const Fig_00_Ext_08& fig08) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(m_streamComponentsMap.empty()) {
        std::cout << m_logTag << " FIG 00 Ext 08 Maps still empty" << std::endl;
        return;
    }

    for(const auto& srvGlobalDef : fig08.getGLobalDefinitions()) {
        if(srvGlobalDef.isShortForm) {
            auto subChanIter = m_streamComponentsMap.find(srvGlobalDef.subchannelId);
            if(subChanIter != m_streamComponentsMap.cend()) {
                //std::cout << m_logTag << " FIG 00 Ext 08 setting ScIdS for SId: " << std::hex << +srvGlobalDef.serviceId << ", SubChanId: " << +srvGlobalDef.subchannelId << ", SubChanId: " << +subChanIter->second->getSubChannelId() << " to: " << +srvGlobalDef.scIdS << std::dec << std::endl;
                subChanIter->second->setServiceComponentIdWithinService(srvGlobalDef.scIdS);
            }
        } else {
            auto packCompIter = m_packetComponentsMap.find(srvGlobalDef.serviceComponentId);
            if(packCompIter != m_packetComponentsMap.cend()) {
                //std::cout << m_logTag << " FIG 00 Ext 08 setting ScIdS for SId: " << std::hex << +srvGlobalDef.serviceId << ", ScId: " << +srvGlobalDef.serviceComponentId << ", SubChanId: " << +packCompIter->second->getSubChannelId() << " to: " << +srvGlobalDef.scIdS << std::dec << std::endl;
                packCompIter->second->setServiceComponentIdWithinService(srvGlobalDef.scIdS);
            }
        }
    }
}

void DabEnsemble::fig00_09_input(const Fig_00_Ext_09& fig09) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if (m_ensembleEcc != fig09.getEnsembleEcc()) {
        std::cout << m_logTag << " Ensemble ECC: " << std::hex << +fig09.getEnsembleEcc()
                  << std::dec << std::endl;
        m_ensembleEcc = fig09.getEnsembleEcc();
    }
}

void DabEnsemble::fig00_10_input(const Fig_00_Ext_10& fig10) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //std::cout << m_logTag << " DateAndTime: " << +fig10.getDabTime().unixTimestampSeconds << std::endl;

    m_dateAndTimeDispatcher.invoke(fig10.getDabTime());
}

void DabEnsemble::fig00_13_input(const Fig_00_Ext_13& fig13) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    for(const auto& uAppInfo : fig13.getUserApplicationInformations()) {
        auto serviceIter = m_servicesMap.find(uAppInfo.serviceID);
        if(serviceIter != m_servicesMap.end()) {
            for(const auto& component : (*serviceIter).second.getServiceComponents()) {
                if(component->getServiceComponentIdWithinService() == uAppInfo.scIdS) {
                    for(const auto& uApp : uAppInfo.userApplications) {
                        std::cout << m_logTag << " FIG 00 Ext 13 Adding UserApplication: " << uApp.userAppType << std::endl;
                        DabUserApplication userApp;
                        userApp.setCaOrganization(uApp.caOrganization);
                        userApp.setDataServiceComonentType(uApp.dataServiceComponentType);
                        userApp.setIsCaApplied(uApp.caApplied);
                        userApp.setIsDataGroupsUsed(uApp.dataGroupsUsed);
                        userApp.setUserApplicationData(uApp.userAppData);
                        userApp.setUserApplicationType(uApp.userAppType);
                        userApp.setXpadAppType(uApp.xpadAppType);
                        userApp.setIsXpadApp(uApp.isXpadData);

                        component->addUserApplication(userApp);
                    }
                }
            }
        } else {
            std::cout << m_logTag << " SId 0x " << std::hex << +uAppInfo.serviceID << std::dec << " not found" << std::endl;
        }
    }
}

void DabEnsemble::fig00_14_input(const Fig_00_Ext_14& fig14) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    if(m_packetComponentsMap.empty()) {
        return;
    }

    for(const auto& fecDesc : fig14.getFecSchemeDescriptions()) {
        if(fecDesc.fecScheme == Fig_00_Ext_14::FEC_SCHEME::APPLIED) {
            auto packCompIter = m_packetComponentsMap.cbegin();
            while(packCompIter != m_packetComponentsMap.cend()) {
                if(packCompIter->second->getSubChannelId() == fecDesc.subChannelId) {
                    packCompIter->second->setIsFecSchemeApplied(true);
                    break;
                }
                packCompIter++;
            }
        }
    }
}

void DabEnsemble::fig00_17_input(const Fig_00_Ext_17& fig17) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    for(const auto& ptyInfo : fig17.getProgrammeTypeInformations()) {
        auto serviceIter = m_servicesMap.find(ptyInfo.serviceId);
        if(serviceIter != m_servicesMap.cend()) {
            serviceIter->second.setProgrammeTypeIsDynamic(ptyInfo.isDynamic);
            serviceIter->second.setProgrammeTypeCode(ptyInfo.intPtyCode);
        }
    }
}


void DabEnsemble::fig00_18_input(const Fig_00_Ext_18& fig18) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //TODO Announcement support indication
    /*
    for(const auto& aSup : fig18.getAnnouncementSupports()) {
        std::cout << "Fig_00_Ext_18 for SId: 0x" << std::hex << +aSup.serviceId << std::dec
        << " with ClusterIds: ";
        for(const auto& clId : aSup.clusterIds) {
            std::cout << "0x" << std::hex << +clId << std::dec << ", ";
        }
         std::cout << "received" << std::endl;
    }
    */
}

void DabEnsemble::fig00_19_input(const Fig_00_Ext_19& fig19) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    for(const auto& aSwitched : fig19.getSwitchedAnnouncements()) {
        if(aSwitched.isNewlyIntroduced) {
            //std::cout << m_logTag << " Announcement for ClusterId: 0x" << std::hex << +aSwitched.clusterId << std::dec << " received. SwitchSize: " << +aSwitched.announcementsSwitched.size() << std::endl;
        }
        auto switchedIter = m_activeAnnouncements.find(aSwitched.clusterId);
        if(switchedIter != m_activeAnnouncements.cend()) {
            //std::cout << m_logTag << " Announcement for ClusterId: 0x" << std::hex << +aSwitched.clusterId << std::dec << " already in map, newly: " <<
            //std::boolalpha << aSwitched.isNewlyIntroduced << std::noboolalpha << ", AnnounceSize: " << +aSwitched.announcementsSwitched.size() << std::endl;
            //if(!aSwitched.isNewlyIntroduced) {
            //Announcement in list, switched-list is empty....announcement ended
            if(aSwitched.announcementsSwitched.empty()) {
                //std::cout << m_logTag << " Announcement for ClusterId: 0x" << std::hex << +(*switchedIter).second.clusterId << std::dec << " ended, removing from map" << std::endl;
                m_activeAnnouncements.erase(switchedIter);
            }
            //}
        } else {
            if(aSwitched.isNewlyIntroduced) {
                if(!aSwitched.announcementsSwitched.empty()) {
                    //std::cout << m_logTag << " Adding newlyIntroduced Announcement with ClusterId: 0x" << std::hex << +aSwitched.clusterId  << std::dec << std::endl;
                    m_activeAnnouncements.insert(std::make_pair(aSwitched.clusterId, aSwitched));
                } else {
                    //std::cout << m_logTag << " Not adding newlyIntroduced Announcement with ClusterId: 0x" << std::hex << +aSwitched.clusterId  << std::dec << " , switched is empty" << std::endl;
                }
            }
        }
    }
}

void DabEnsemble::fig01_00_input(const Fig_01_Ext_00& fig10) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " ServiceSanity FIC 01_00: " << std::hex << +fig10.getEnsembleId() << std::dec
              << " short: " << fig10.getEnsembleShortLabel()
              << " long: " << fig10.getEnsembleLabel() << std::endl;
    if(m_ensembleLabel.empty() && m_ensembleShortLabel.empty()) {
        if(fig10.getEnsembleId() == m_ensembleId) {
            m_ensembleLabel = fig10.getEnsembleLabel();
            m_ensembleShortLabel = fig10.getEnsembleShortLabel();
            m_labelCharset = fig10.getCharset();
        }
    }
}

void DabEnsemble::fig01_01_input(const Fig_01_Ext_01& fig11) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " ServiceSanity FIC 01_01: " << std::hex << +fig11.getProgrammeServiceId() << std::dec << " : " << fig11.getProgrammeServiceLabel() << std::endl;
    auto serviceIter = m_servicesMap.find(fig11.getProgrammeServiceId());
    if(serviceIter != m_servicesMap.cend()) {
        serviceIter->second.setLabelCharset(fig11.getCharset());
        serviceIter->second.setServiceLabel(fig11.getProgrammeServiceLabel());
        serviceIter->second.setServiceShortLabel(fig11.getProgrammeServiceShortLabel());
    }
}

void DabEnsemble::fig01_04_input(const Fig_01_Ext_04& fig14) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " ServiceSanity FIC 01_04: " << std::hex << +fig14.getServiceId() << std::dec << ", SCIdS: " << +fig14.getServiceComponentIdWithinService() << " : " << fig14.getServiceComponentLabel() << std::endl;
    auto serviceIter = m_servicesMap.find(fig14.getServiceId());
    if(serviceIter != m_servicesMap.cend()) {
        for(auto& component : serviceIter->second.getServiceComponents()) {
            if(component->getServiceComponentIdWithinService() == fig14.getServiceComponentIdWithinService()) {
                component->setLabelCharset(fig14.getCharset());
                component->setServiceComponentLabel(fig14.getServiceComponentLabel());
                component->setServiceComponentShortLabel(fig14.getServiceComponentShortLabel());
            }
        }
    }
}

void DabEnsemble::fig01_05_input(const Fig_01_Ext_05& fig15) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " ServiceSanity FIC 01_05: " << std::hex << +fig15.getDataServiceId() << std::dec << " : " << fig15.getDataServiceLabel() << std::endl;
    auto serviceIter = m_servicesMap.find(fig15.getDataServiceId());
    if(serviceIter != m_servicesMap.cend()) {
        serviceIter->second.setLabelCharset(fig15.getCharset());
        serviceIter->second.setServiceLabel(fig15.getDataServiceLabel());
        serviceIter->second.setServiceShortLabel(fig15.getDataServiceShortLabel());
    }
}

void DabEnsemble::fig01_06_input(const Fig_01_Ext_06& fig16) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);

}


void DabEnsemble::fig_00_done_cb(Fig::FIG_00_TYPE type) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    //std::cout << m_logTag << " FIG 00 Extension: " << +type << " done" << std::endl;

    switch (type) {
        case Fig::FIG_00_TYPE::ENSEMBLE_INFORMATION: {
            if (!m_fig000done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_00 Done!" << std::endl;
                m_fig000done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::BASIC_SUBCHANNEL_ORGANIZATION: {
            if(!m_fig001done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_01 Done!" << std::endl;
                m_fig001done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::BASIC_SERVICE_COMPONENT_DEFINITION: {
            if(!m_fig002done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_02 Done!" << std::endl;
                m_fig002done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_PACKET_MODE: {
            if(!m_fig003done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_03 Done!" << std::endl;
                m_fig003done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::SERVICE_COMPONENT_GLOBAL_DEFINITION: {
            if(!m_fig008done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_08 Done!" << std::endl;
                m_fig008done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::USERAPPLICATION_INFORMATION: {
            if (!m_fig013done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_08 Done!" << std::endl;
                m_fig013done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::FEC_SUBCHANNEL_ORGANIZATION: {
            if (!m_fig014done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_14 Done!" << std::endl;
                m_fig014done = true;
            }
            break;
        }
        case Fig::FIG_00_TYPE::PROGRAMME_TYPE: {
            if (m_fig017done) {
                std::cout << m_logTag << " ServiceSanity FIC 00_14 Done!" << std::endl;
                m_fig017done = true;
            }
            break;
        }
        default:
            break;
    }
    std::stringstream logmsg;
    logmsg << m_logTag << " FIGs ";
    logmsg << "0/0:" << m_fig000done << ", ";
    logmsg << "0/1:" << m_fig001done << ", ";
    logmsg << "0/2:" << m_fig002done << ", ";
    logmsg << "0/8:" << m_fig008done << ", ";
    logmsg << "0/13:" << m_fig013done << ", ";
    logmsg << "1/0:" << m_fig100done << ", ";
    logmsg << "1/1:" << m_fig101done;
    std::cout << logmsg.str() << std::endl;

    /** ETSI EN 300 401 V2.1.1, 6.3 Service organization, 6.3.0 Service organization
     * The service organization defines the services and service components carried in the ensemble.
     * It is coded in the Extensions 2, 3, 4, 8 and 13 of FIG type 0
     *
     * Implementation note:
     * FIG 0/3 not needed (for packet mode only)
     * FIG 0/4 not needed (no support for CA)
     * FIG 1/0 needed for ensemble label
     * FIG 1/1 needed for service label
     */
    if(m_fig000done && m_fig001done && m_fig002done && m_fig008done && m_fig013done) {
        m_fig0done = true;
        if (m_fig100done && m_fig101done) {
            // Note: A Tuner may override this to apply sanity check only to a specific service
            checkServiceSanity(DabService::SID_INVALID);
        }
    }
}

void DabEnsemble::fig_01_done_cb(Fig::FIG_01_TYPE type) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    std::cout << m_logTag << " FIG 01 Extension: " << +type << " done" << std::endl;
    switch (type) {
        case Fig::FIG_01_TYPE::ENSEMBLE_LABEL: {
            m_fig100done = true;
            break;
        }
        case Fig::FIG_01_TYPE::PROGRAMME_SERVICE_LABEL: {
            m_fig101done = true;
            break;
        }
        case Fig::FIG_01_TYPE::SERVICE_COMPONENT_LABEL: {
            m_fig104done = true;
            break;
        }
        case Fig::FIG_01_TYPE::DATA_SERVICE_LABEL: {
            m_fig105done = true;
            break;
        }
        case Fig::FIG_01_TYPE::XPAD_USERAPPLICATION_LABEL: {
            m_fig106done = true;
            break;
        }
        default: {
            break;
        }
    }

    //TODO packetcomponent label not sent for MOT servicecomponent in own subchannel
    if(m_fig100done && m_fig101done) {
        m_fig1done = true;
    }
}

void DabEnsemble::checkServiceSanity(const uint32_t serviceId ) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);

    if (serviceId != DabService::SID_INVALID) {
        // find specific service and check its sanity
        const auto & iter = m_servicesMap.find(serviceId);
        if (iter != m_servicesMap.cend()) {
            if (! iter->second.checkSanity() ){
                std::cout << m_logTag << "checkServiceSanity failed for SId 0x" << std::hex << +serviceId << std::dec << std::endl;
                return;
            }
        } else {
            // cannot find specific service
            std::cout << m_logTag << "checkServiceSanity failed to find SId 0x" << std::hex << +serviceId << std::dec << std::endl;
            return;
        }
    } else {
        // check all services in ensemble
        for (const auto &srvMapEntry : m_servicesMap) {
            bool wasSane = srvMapEntry.second.checkSanity();
            if (wasSane && srvMapEntry.second.isProgrammeService()) {
                uint32_t numPrimAudioComponents = 0;
                for (const auto &srvCmp : srvMapEntry.second.getServiceComponents()) {
                    if (srvCmp->isPrimary()) {
                        numPrimAudioComponents++;
                    }
                }
                if (numPrimAudioComponents > 1) {
                    std::clog << m_logTag << "checkServiceSanity for SId 0x" << std::hex
                              << +srvMapEntry.second.getServiceId()
                              << ": num primary audio stream components: "
                              << +numPrimAudioComponents << std::endl;
                }
            }
            if (!wasSane) {
                std::cout << m_logTag << "checkServiceSanity failed to find SId 0x" << std::hex
                          << +srvMapEntry.second.getServiceId() << std::dec << std::endl;
                return;
            }
        }
    }
    // service(s) checked, now ensemble itself
    std::stringstream logStr;
    logStr << m_logTag << "checkServiceSanity failed EId=0x" << std::hex << +getEnsembleId() << std::dec;
    if (getEnsembleEcc() == ECC_INVALID || getEnsembleId() == EID_INVALID
       || getEnsembleLabelCharset() == CHARSET_INVALID || getEnsembleLabel().empty() || getEnsembleShortLabel().empty()) {
        logStr << " ECC:0x" << std::hex << +getEnsembleEcc() << std::dec << " '" << getEnsembleLabel() << "', '"
               << getEnsembleShortLabel() << "' charset:0x" << std::hex << +getEnsembleLabelCharset() << std::dec;
        std::cout << logStr.str() << std::endl;
        return;
    }

    logStr.str(std::string());
    logStr << m_logTag << " checkServiceSanity passed '" << getEnsembleLabel() << "'";
    if (serviceId != DabService::SID_INVALID) {
        logStr << " for SId 0x" << std::hex << +serviceId << std::dec;
    }
    std::cout << logStr.str() << std::endl;

    if (!m_ensembleCollectFinished) {
        m_ensembleCollectFinished = true;

        unregisterCbsAfterEnsembleCollect();

        if (!m_ensembleCollectDoneDispatcher.hasCallbacks()) {
            std::clog << m_logTag << " EnsembleCollectDone has no callbacks" << std::endl;
        }
        m_ensembleCollectDoneDispatcher.invoke();
    }
}

std::shared_ptr<std::function<void()>> DabEnsemble::registerEnsembleCollectDoneCallback(std::function<void()> cb) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    return m_ensembleCollectDoneDispatcher.add(cb);
}

std::shared_ptr<DabEnsemble::Date_Time_Callback> DabEnsemble::registerDateTimeCallback(DabEnsemble::Date_Time_Callback cb) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    return m_dateAndTimeDispatcher.add(cb);
}

std::shared_ptr<DabEnsemble::ServiceFollowingCallback> DabEnsemble::registerServiceFollowingCallback(DabEnsemble::ServiceFollowingCallback cb) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    return m_serviceFollowingDispatcher.add(cb);
}

void DabEnsemble::lookupEIdOnOtherFrequency(
        // Inputs
        const uint32_t targetEId, const uint32_t targetFreqKHz,
        const uint8_t targetECC, const uint32_t targetSId,
        // outputs
        std::vector<std::shared_ptr<LinkedServiceDab>> & retAdjacentFrequencies,
        std::vector<std::shared_ptr<LinkedServiceDab>> & retNotAdjacentFrequencies) const {

    retAdjacentFrequencies.clear();
    retNotAdjacentFrequencies.clear();

    /*
     * ETSI TS 103 176 V2.4.1, 5.6.3.2 Stage 1: Find same service
     *
     * In this stage, the receiver attempts to locate a DAB ensemble containing a service with ECC-SId equal to the Target Id.
     *
     * It may use FI (FIG 0/21) to find the same ensemble on another frequency
     */

    std::cout << m_logTag << " Lookup EId 0x" << std::hex << +targetEId << std::dec
              << " on another frequency, frequencyInformationDb (size "
              << +m_frequencyInformationDb.size() << ")" << std::endl;

    for (const auto &freqInfoDbEntry : m_frequencyInformationDb) {
        for (const auto &freqInfo : freqInfoDbEntry.second) {
            if (freqInfo.frequencyInformationType == Fig_00_Ext_21::DAB_ENSEMBLE &&
                freqInfo.id == targetEId) {
                for (const auto &fli : freqInfo.frequencies) {

                    // searching for a different frequency than the frequency of the current service
                    if (targetFreqKHz != fli.frequencyKHz) {
                        auto servicePtr = std::make_shared<LinkedServiceDab>();
                        servicePtr->setEnsembleEcc(targetECC);
                        servicePtr->setEnsembleId(targetEId);
                        servicePtr->setEnsembleFrequencyKHz(fli.frequencyKHz);
                        servicePtr->setServiceId(targetSId);

                        std::cout << m_logTag << "   match: 0x" << std::hex << +freqInfo.id
                                  << std::dec
                                  << " type " << +freqInfo.frequencyInformationType
                                  << " : ecc 0x" << std::hex << +servicePtr->getEnsembleEcc()
                                  << ",sid 0x" << +servicePtr->getServiceId()
                                  << ",eid 0x" << +servicePtr->getEnsembleId()
                                  << std::dec
                                  << "," << +servicePtr->getEnsembleFrequencyKHz() << std::dec
                                  << " kHz" << std::endl;

                        if (fli.additionalInfo.dabEnsembleAdjacent ==
                            Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_UNKNOWN ||
                            fli.additionalInfo.dabEnsembleAdjacent ==
                            Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED ||
                            fli.additionalInfo.dabEnsembleAdjacent ==
                            Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE) {
                            // adjacent services
                            retAdjacentFrequencies.push_back(servicePtr);
                        } else {
                            // not adjacent services
                            retNotAdjacentFrequencies.push_back(servicePtr);
                        }
                    } else {
                        std::cout << m_logTag << "   skip'd same frequency match EId 0x"
                                  << std::hex << +freqInfo.id << std::dec
                                  << " : " << +fli.frequencyKHz << " kHz" << std::endl;
                    }
                }
            } else {
                std::cout << m_logTag << "   no match: 0x" << std::hex << +freqInfo.id << std::dec
                          << " type " << +freqInfo.frequencyInformationType << std::endl;
            }
        }
    }
}

void DabEnsemble::lookupOtherEnsembleSameService(
        // Inputs
        const uint32_t targetEId, const uint32_t targetFreqKHz,
        const uint8_t targetECC, const uint32_t targetSId,
        // Outputs
        std::vector<std::shared_ptr<LinkedServiceDab>> & sameSIdOtherEnsembles ) const {

    sameSIdOtherEnsembles.clear();

    /*
     *  ETSI TS 103 176 V2.4.1, 5.6.3.2 Stage 1: Find same service
     *
     * It may use [...] OE Services information (FIG 0/24) and FI to find another ensemble carrying the same service
     */
    std::cout << m_logTag << "Lookup other ensembles carrying sid 0x" << std::hex << +targetSId << std::dec
              << " oeSrvInfoDb (size " << +m_oeSrvInfoDb.size() << ")" << std::endl;

    for (const auto & oeSrvInfo : m_oeSrvInfoDb) {
        for (const auto & oe : oeSrvInfo.second) {
            if (oe.serviceId == targetSId) {
                // found same service in other ensemble
                for (const auto & otherEId : oe.ensembleIds) {
                    if (otherEId != targetEId) {
                        // lookup frequencies of other ensemble
                        std::cout << m_logTag << "   match: sid 0x" << std::hex << +targetSId
                                  << ",eid 0x" << +otherEId << std::dec << std::endl;

                        std::vector<std::shared_ptr<LinkedServiceDab>> adjacentFrequencies;
                        std::vector<std::shared_ptr<LinkedServiceDab>> notAdjacentFrequencies;
                        lookupEIdOnOtherFrequency(otherEId, targetFreqKHz, targetECC, targetSId,
                                                  adjacentFrequencies, notAdjacentFrequencies);

                        for (const auto & a : adjacentFrequencies) {
                            // avoid duplicates (yeah it is not the most efficient way to do this)
                            if (std::find(sameSIdOtherEnsembles.begin(),
                                          sameSIdOtherEnsembles.end(), a) == sameSIdOtherEnsembles.end()) {
                                sameSIdOtherEnsembles.push_back(a);
                            }
                        }
                        for (const auto & a : notAdjacentFrequencies) {
                            // avoid duplicates (yeah it is not the most efficient way to do this)
                            if (std::find(sameSIdOtherEnsembles.begin(),
                                          sameSIdOtherEnsembles.end(), a) == sameSIdOtherEnsembles.end()) {
                                sameSIdOtherEnsembles.push_back(a);
                            }
                        }
                    }
                }
            } else {
                std::cout << m_logTag << "   no match: SId 0x" << std::hex << +oe.serviceId << std::dec << std::endl;
            }
        }
    }
}

void DabEnsemble::lookupHardLinksToService(
        // Inputs
        const uint32_t targetEId, const uint32_t targetFreqKHz,
        const uint8_t targetECC, const uint32_t targetSId,
        // Outputs
        std::vector<std::shared_ptr<LinkedServiceDab>> & hardLinksToService ) const {

    hardLinksToService.clear();

    /*
     *  ETSI TS 103 176 V2.4.1, 5.6.3.3 Stage 2: Follow hard links
     *
     *  The receiver next attempts to find a service that is hard-linked to the Target Id and is
     *  therefore carrying identical audio content to the Target Id.
     *
     *  2.1 Hard Links to DAB service
     *
     *  The receiver uses linkage information received for the selected service to assemble a list
     *  of Candidate Ids from the activated linkage sets. From that list the receiver attempts to
     *  identify ensembles and frequencies that carry a linked DAB service using its information base.
     */
    std::cout << m_logTag << " Lookup linkage info for sid 0x" << std::hex << +targetSId << std::dec
              << " m_serviceLinkDb (size " << +m_serviceLinkDb.size() << ")" << std::endl;

    for (const auto & serviceLink : m_serviceLinkDb) {
        const auto & linkInfo = serviceLink.second;
        if (linkInfo.linkageActive) {
            if (!linkInfo.isSoftLink) {
                int i = 0;
                for (const auto & link : linkInfo.serviceLinks) {
                    if (link.idListQualifier == Fig_00_Ext_06::DAB_SID) {
                        if (link.containsId(targetSId)) {
                            /* 5.6.4 Selection procedure
                             * EXAMPLE: Stage 2: [...] As SId exists in the list, any of the other services
                             * in the list are possible alternatives that will provide the same
                             * audio if this linkage set is active at the moment of selection.
                             */

                            for (const auto & candidateSid : link.idList) {
                                if (candidateSid != targetSId) {
                                    std::cout << m_logTag << "   candidate DAB SId 0x"
                                              << +candidateSid
                                              << " in linkageSetNumber 0x" << std::hex
                                              << +linkInfo.linkageSetNumber << ",keySId 0x"
                                              << +linkInfo.keyServiceId
                                              << std::dec << ", at serviceLinks[" << +i << "]"
                                              << std::endl;
                                    std::vector<std::shared_ptr<LinkedServiceDab>> otherEnsemblesOtherSId;
                                    lookupOtherEnsembleSameService(targetEId, targetFreqKHz,
                                                                   targetECC, candidateSid, otherEnsemblesOtherSId);
                                    for (const auto & a : otherEnsemblesOtherSId) {
                                        // avoid duplicates (yeah it is not the most efficient way to do this)
                                        if (std::find(hardLinksToService.begin(),
                                                      hardLinksToService.end(), a) == hardLinksToService.end()) {
                                            hardLinksToService.push_back(a);
                                        }
                                    }
                                }
                            }
                        } else {
                            std::cout << m_logTag << "   not contains DAB SId 0x" << std::hex << +targetSId
                                      << ": linkageSetNumber 0x" << +linkInfo.linkageSetNumber
                                      << ",keySId 0x" << +linkInfo.keyServiceId
                                      << std::dec << ", serviceLinks[" << +i << "]" << std::endl;
                        }
                    } else {
                        std::cout << m_logTag << "   not DAB: linkageSetNumber 0x" << std::hex
                                  << +linkInfo.linkageSetNumber << ",keySId 0x" << +linkInfo.keyServiceId
                                  << std::dec << ", serviceLinks[" << +i << "]" << std::endl;
                    }
                    i++;
                }
            } else {
                std::cout << m_logTag << "   not hardlink: linkageSetNumber 0x" << std::hex
                          << +linkInfo.linkageSetNumber << ",keySId 0x" << +linkInfo.keyServiceId
                          << std::dec << std::endl;
            }
        } else {
            std::cout << m_logTag << "   not active: linkageSetNumber 0x" << std::hex
                      << +linkInfo.linkageSetNumber << ", keySId 0x" << +linkInfo.keyServiceId
                      << std::dec << std::endl;
        }
    }
}

std::vector<std::shared_ptr<LinkedServiceDab>> DabEnsemble::getLinkedDabServices(const LinkedServiceDab & service) {
    std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);

    // dump databases
    dumpFrequencyDb();
    dumpOeSrvInfoDb();
    dumpServiceLinkDb();

    const auto targetECC = service.getEnsembleEcc();
    const auto targetEId = service.getEnsembleId();
    const auto targetFreqKHz = service.getEnsembleFrequencyKHz();
    const auto targetSId = service.getServiceId();

    std::vector<std::shared_ptr<LinkedServiceDab>> collectedServices;

    // Stage 1: Find same service: find the same ensemble on another frequency
    {
        std::vector<std::shared_ptr<LinkedServiceDab>> otherFrequenciesAdjacent;
        std::vector<std::shared_ptr<LinkedServiceDab>> otherFrequenciesNotAdjacent;

        lookupEIdOnOtherFrequency(targetEId, targetFreqKHz, targetECC, targetSId,
                                  otherFrequenciesAdjacent, otherFrequenciesNotAdjacent);

        for (const auto &a : otherFrequenciesAdjacent) {
            // avoid duplicates (yeah it is not the most efficient way to do this)
            if (std::find(collectedServices.begin(),
                          collectedServices.end(), a) == collectedServices.end()) {
                collectedServices.push_back(a);
            }
        }
        for (const auto &a : otherFrequenciesNotAdjacent) {
            // avoid duplicates (yeah it is not the most efficient way to do this)
            if (std::find(collectedServices.begin(),
                          collectedServices.end(), a) == collectedServices.end()) {
                collectedServices.push_back(a);
            }
        }
    }

    // Stage 1: Find same service: find another ensemble carrying the same service
    {
        std::vector<std::shared_ptr<LinkedServiceDab>> sameSIdOtherEnsembles;
        lookupOtherEnsembleSameService(targetEId, targetFreqKHz, targetECC, targetSId,
                                       sameSIdOtherEnsembles);

        for (const auto &a : sameSIdOtherEnsembles) {
            // avoid duplicates (yeah it is not the most efficient way to do this)
            if (std::find(collectedServices.begin(),
                          collectedServices.end(), a) == collectedServices.end()) {
                collectedServices.push_back(a);
            }
        }
    }

    // Stage 2: Follow hard links
    {
        std::vector<std::shared_ptr<LinkedServiceDab>> hardLinksToService;
        lookupHardLinksToService(targetEId, targetFreqKHz, targetECC, targetSId,
                                 hardLinksToService);

        for (const auto &a : hardLinksToService) {
            // avoid duplicates (yeah it is not the most efficient way to do this)
            if (std::find(collectedServices.begin(),
                          collectedServices.end(), a) == collectedServices.end()) {
                collectedServices.push_back(a);
            }
        }
    }

    /*
     * 5.6.3.3 Stage 2 Follow hard links
     * State 2.2 Hard links to FM or other bearers
     *   ===> is not implemented and no plan to do so
     */

    /*
     * 5.6.3.4 Stage 3: Follow soft links
     *   ===> is not implemented and no plan to do so
     */

    std::cout << m_logTag << "collected " << collectedServices.size() << " linked services" << std::endl;
    return collectedServices;
}

