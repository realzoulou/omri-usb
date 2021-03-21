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

#ifndef DABENSEMBLE_H
#define DABENSEMBLE_H

#include <vector>
#include <string>
#include <map>
#include <memory>
#include <mutex>

#include "ficparser.h"

#include "dabservicecomponentmscpacketdata.h"
#include "dabservicecomponentmscstreamaudio.h"
#include "dabservicecomponentmscstreamdata.h"
#include "dabservice.h"
#include "linkedservicedab.h"

class DabEnsemble {

public:
    static constexpr uint32_t FREQ_INVALID = 0;
    static constexpr uint32_t EID_INVALID = 0x0000FFFF;
    static constexpr uint8_t ECC_INVALID = 0xFF;
    static constexpr uint8_t CHARSET_INVALID = 0xFF;

public:
    explicit DabEnsemble();
    virtual ~DabEnsemble();

    virtual void reset();

    virtual uint16_t getEnsembleId() const;
    virtual uint8_t getCurrentCifCountHigh() const;
    virtual uint8_t getCurrentCifCountLow() const;
    virtual bool isAlarmAnnouncementSupported() const;

    virtual uint8_t getEnsembleLabelCharset() const;
    virtual std::string getEnsembleLabel() const;
    virtual std::string getEnsembleShortLabel() const;

    virtual uint8_t getEnsembleEcc() const;

    virtual std::vector<std::shared_ptr<DabService>> getDabServices();
    virtual std::vector<std::shared_ptr<LinkedServiceDab>> getLinkedDabServices(const LinkedServiceDab & service);

    virtual std::shared_ptr<std::function<void()>> registerEnsembleCollectDoneCallback(std::function<void()> cb);

    using Date_Time_Callback = std::function<void(const Fig_00_Ext_10::DabTime&)>;
    virtual std::shared_ptr<DabEnsemble::Date_Time_Callback> registerDateTimeCallback(Date_Time_Callback cb);

    using ServiceFollowingCallback = std::function<void()>;
    virtual std::shared_ptr<DabEnsemble::ServiceFollowingCallback> registerServiceFollowingCallback(DabEnsemble::ServiceFollowingCallback cb);

protected:
    virtual void dataInput(const std::vector<uint8_t>& data, uint8_t subChId, bool synchronized, bool rfLock = true);
    virtual void flushBufferedComponentData(uint8_t subChId);
    virtual void flushAllBufferedComponentData();

    std::unique_ptr<FicParser> m_ficPtr{nullptr};
    uint32_t m_ensembleFrequency{FREQ_INVALID};
    bool m_ensembleCollectFinished{false};

    // mutex to guard read/write access to internal data
    // use it by placing following code as (typ.) first instruction of a method:
    //   std::lock_guard<std::recursive_mutex> lockGuard(m_mutex);
    // by leaving the method, the mutex is automatically released
    std::recursive_mutex m_mutex;

    virtual void checkServiceSanity(const uint32_t serviceId);

private:
    void registerCbs();
    void unregisterCbsAfterEnsembleCollect();

    void fig00_00_input(const Fig_00_Ext_00& fig00);
    void fig00_01_input(const Fig_00_Ext_01& fig01);
    void fig00_02_input(const Fig_00_Ext_02& fig02);
    void fig00_03_input(const Fig_00_Ext_03& fig03);

    //ServiceFollowing
    void fig00_06_input(const Fig_00_Ext_06& fig06);
    void fig00_21_input(const Fig_00_Ext_21& fig21);
    void fig00_24_input(const Fig_00_Ext_24& fig24);

    void fig00_08_input(const Fig_00_Ext_08& fig08);
    void fig00_09_input(const Fig_00_Ext_09& fig09);
    void fig00_10_input(const Fig_00_Ext_10& fig10);
    void fig00_13_input(const Fig_00_Ext_13& fig13);
    void fig00_14_input(const Fig_00_Ext_14& fig14);
    void fig00_17_input(const Fig_00_Ext_17& fig17);

    //Announcements
    void fig00_18_input(const Fig_00_Ext_18& fig18);
    void fig00_19_input(const Fig_00_Ext_19& fig19);

    void fig01_00_input(const Fig_01_Ext_00& fig10);
    void fig01_01_input(const Fig_01_Ext_01& fig11);
    void fig01_04_input(const Fig_01_Ext_04& fig14);
    void fig01_05_input(const Fig_01_Ext_05& fig15);
    void fig01_06_input(const Fig_01_Ext_06& fig16);

private:
    const std::string m_logTag = "[DabEnsemble]";

    std::shared_ptr<FicParser::Fig_00_Done_Callback> m_00DonePtr;
    void fig_00_done_cb(Fig::FIG_00_TYPE type);

    std::shared_ptr<FicParser::Fig_01_Done_Callback> m_01DonePtr;
    void fig_01_done_cb(Fig::FIG_01_TYPE type);

    std::shared_ptr<std::function<void (const Fig_00_Ext_00&)>> m_00Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_01&)>> m_01Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_02&)>> m_02Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_03&)>> m_03Ptr;

    std::shared_ptr<std::function<void (const Fig_00_Ext_08&)>> m_08Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_09&)>> m_09Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_10&)>> m_010Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_13&)>> m_013Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_14&)>> m_014Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_17&)>> m_017Ptr;

    std::shared_ptr<std::function<void (const Fig_01_Ext_00&)>> m_10Ptr;
    std::shared_ptr<std::function<void (const Fig_01_Ext_01&)>> m_11Ptr;
    std::shared_ptr<std::function<void (const Fig_01_Ext_04&)>> m_14Ptr;
    std::shared_ptr<std::function<void (const Fig_01_Ext_05&)>> m_15Ptr;
    std::shared_ptr<std::function<void (const Fig_01_Ext_06&)>> m_16Ptr;

    //ServiceFollowing
    std::shared_ptr<std::function<void (const Fig_00_Ext_06&)>> m_06Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_21&)>> m_21Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_24&)>> m_24Ptr;

    //Announcements
    std::shared_ptr<std::function<void (const Fig_00_Ext_18&)>> m_18Ptr;
    std::shared_ptr<std::function<void (const Fig_00_Ext_19&)>> m_19Ptr;

private:
    bool m_isInitializing{false};
    std::atomic<bool> m_resetting{false};
    std::chrono::steady_clock::time_point m_ensembleCollectStartTime;

    //FIG 0/1 information
    uint16_t m_ensembleId{EID_INVALID};
    uint8_t m_cifCntHigh{0x00};
    uint8_t m_cifCntLow{0x00};
    uint8_t m_cifCntHighNext{0x00};
    uint8_t m_cifCntLowNext{0x00};
    bool m_announcementsSupported{false};

    uint8_t m_ensembleEcc{ECC_INVALID};

    //FIG 1/0
    uint8_t m_labelCharset{CHARSET_INVALID};
    std::string m_ensembleLabel{""};
    std::string m_ensembleShortLabel{""};

    //Service information mappings
    std::map<uint32_t, DabService> m_servicesMap;
    std::map<uint8_t, std::shared_ptr<DabServiceComponent>> m_streamComponentsMap;
    std::map<uint16_t, std::shared_ptr<DabServiceComponentMscPacketData>> m_packetComponentsMap;

    CallbackDispatcher<ServiceFollowingCallback> m_serviceFollowingDispatcher;

    CallbackDispatcher<std::function<void()>> m_ensembleCollectDoneDispatcher;

    CallbackDispatcher<Date_Time_Callback> m_dateAndTimeDispatcher;

    bool m_fig000done{false};
    bool m_fig001done{false};
    bool m_fig002done{false};
    bool m_fig003done{false};
    bool m_fig008done{false};
    bool m_fig013done{false};
    bool m_fig014done{false};
    bool m_fig017done{false};
    bool m_fig0done{false};

    bool m_fig100done{false};
    bool m_fig101done{false};
    bool m_fig104done{false};
    bool m_fig105done{false};
    bool m_fig106done{false};
    bool m_fig1done{false};

protected:
    //Service Following DB
    std::map<uint16_t, Fig_00_Ext_06::ServiceLinkingInformation> m_serviceLinkDb;
    std::map<uint16_t, std::vector<Fig_00_Ext_21::FrequencyInformation>> m_frequencyInformationDb;
    std::map<uint16_t, std::vector<Fig_00_Ext_24::OtherEnsembleServiceInformation>> m_oeSrvInfoDb;

private:
    void dumpServiceLinkDb() const;
    void dumpFrequencyDb() const;
    void dumpOeSrvInfoDb() const;

    void lookupEIdOnOtherFrequency( // Inputs
            const uint32_t targetEId, const uint32_t targetFreqKHz,
            const uint8_t targetECC, const uint32_t targetSId,
            // Outputs
            std::vector<std::shared_ptr<LinkedServiceDab>> & retAdjacentFrequencies,
            std::vector<std::shared_ptr<LinkedServiceDab>> & retNotAdjacentFrequencies) const;

    void lookupOtherEnsembleSameService(
            // Inputs
            const uint32_t targetEId, const uint32_t targetFreqKHz,
            const uint8_t targetECC, const uint32_t targetSId,
            // Outputs
            std::vector<std::shared_ptr<LinkedServiceDab>> & sameSIdOtherEnsembles) const;

    void lookupHardLinksToService(
            // Inputs
            const uint32_t targetEId, const uint32_t targetFreqKHz,
            const uint8_t targetECC, const uint32_t targetSId,
            // Outputs
            std::vector<std::shared_ptr<LinkedServiceDab>> & hardLinksToService ) const;


private:
    //Announcements
    std::map<uint8_t, Fig_00_Ext_19::AnnouncementSwitching> m_activeAnnouncements;
    std::map<uint16_t, Fig_00_Ext_18::AnnouncementSupport> m_announcementSupports;
};

#endif // DABENSEMBLE_H
