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

#ifndef DABSERVICECOMPONENT_H
#define DABSERVICECOMPONENT_H

#include <vector>

#include "dabuserapplication.h"

class DabServiceComponent {

public:
    enum SERVICECOMPONENTTYPE {
        MSC_STREAM_AUDIO,
        MSC_STREAM_DATA,
        RESERVED,
        MSC_PACKET_MODE_DATA
    };

    static constexpr uint8_t SUBCHID_INVALID = 0xFFu;
    static constexpr uint8_t SCIDS_INVALID = 0xFFu;
    static constexpr uint16_t MSC_STARTADDR_INVALID = 0xFFFFu;
    static constexpr uint16_t SUBCHAN_SIZE_INVALID = 0xFFFFu;
    static constexpr uint16_t SUBCHAN_BITRATE_INVALID = 0xFFFFu;
    static constexpr uint8_t CHARSET_INVALID = 0xFFu;
    static constexpr uint8_t PROT_LEVEL_INVALID = 0xFFu;
    static constexpr uint8_t PROT_TYPE_INVALID = 0xFFu;
    static constexpr uint8_t UEPTABLEINDEX_INVALID = 0xFFu;

public:
    virtual ~DabServiceComponent();

    virtual DabServiceComponent::SERVICECOMPONENTTYPE getServiceComponentType() const;
    virtual uint8_t getSubChannelId() const;
    virtual bool isPrimary() const;
    virtual bool isCaApplied() const;
    virtual uint8_t getServiceComponentIdWithinService() const;
    virtual uint16_t getMscStartAddress() const;
    virtual uint16_t getSubchannelSize() const;
    virtual std::string getConvolutionalCodingRate() const;
    virtual std::string getProtectionLevelString() const;
    virtual uint8_t getProtectionLevel() const;
    virtual uint8_t getProtectionType() const;
    virtual uint8_t getUepTableIndex() const;
    virtual uint16_t getSubchannelBitrate() const;
    virtual bool isFecSchemeApplied() const;

    virtual uint8_t getLabelCharset() const;
    virtual std::string getServiceComponentLabel() const;
    virtual std::string getServiceComponentShortLabel() const;

    virtual const std::vector<DabUserApplication>& getUserApplications() const;

    virtual void setIsCaApplied(bool caApplied);
    virtual void setIsPrimary(bool primary);
    virtual void setSubchannelId(uint8_t subChanId);
    virtual void setServiceComponentIdWithinService(uint8_t scIdS);

    virtual void setMscStartAddress(uint16_t startAddress);
    virtual void setSubchannelSize(uint16_t subChanSize);
    virtual void setConvolutionalCodingRate(std::string convo);
    virtual void setProtectionLevelString(std::string protLevel);
    virtual void setProtectionLevel(uint8_t protLevel);
    virtual void setProtectionType(uint8_t protType);
    virtual void setUepTableIndex(uint8_t tableIdx);
    virtual void setSubchannelBitrate(uint16_t bitrateKbits);
    virtual void setIsFecSchemeApplied(bool fecApplied);

    virtual void setLabelCharset(uint8_t charset);
    virtual void setServiceComponentLabel(const std::string& label);
    virtual void setServiceComponentShortLabel(const std::string& shortLabel);

    virtual void addUserApplication(const DabUserApplication& uApp);

    virtual void componentMscDataInput(const std::vector<uint8_t>& mscData, bool synchronized) = 0;

    virtual void flushBufferedData();

    virtual bool checkSanity() const;

protected:
    explicit DabServiceComponent();

    DabServiceComponent::SERVICECOMPONENTTYPE m_componentType{SERVICECOMPONENTTYPE::RESERVED};
    bool m_isPrimary{false};
    bool m_isCaApplied{false};
    uint8_t m_subChanId{SUBCHID_INVALID};
    uint8_t m_scIdS{SCIDS_INVALID};
    uint16_t m_mscStartAddress{MSC_STARTADDR_INVALID};
    uint16_t m_subChanSize{SUBCHAN_SIZE_INVALID};
    std::string m_convoCodingRate{""};
    std::string m_protectionLevelString{""};
    uint16_t m_subChanBitrate{SUBCHAN_BITRATE_INVALID};

    uint8_t m_labelCharset{CHARSET_INVALID};
    std::string m_serviceComponentLabel{""};
    std::string m_serviceComponentShortLabel{""};

    std::vector<DabUserApplication> m_userApplications;

    bool m_fecSchemeAplied{false};

    uint8_t m_protectionLevel{PROT_LEVEL_INVALID};
    uint8_t m_protectionType{PROT_TYPE_INVALID};
    uint8_t m_uepTableIdx{UEPTABLEINDEX_INVALID};

private:
    std::string m_logTag = "[DabServiceComponent]";
};

#endif // DABSERVICECOMPONENT_H
