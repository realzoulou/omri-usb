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

#ifndef FIG_00_EXT_24_H
#define FIG_00_EXT_24_H

#include <algorithm>
#include <cstdint>

#include "fig_00.h"

/*
 * ETS 300 401 clause 8.1.10 OE Services
 *
 * The OE Services feature is used to identify the services currently carried in other DAB ensembles.
 * The feature is encoded in Extension 24 of FIG type 0 (FIG 0/24).
 *
 * If the service is carried in the tuned ensemble the OE flag shall be set to "0" If the service is not carried in the tuned
 * ensemble the OE flag shall be set to "1".
 *
 * This feature shall use the SIV signalling (see clause 5.2.2.1). The database shall be divided by use of a database key.
 * Changes to the database shall be signalled using the CEI.
 *
 * OE Services forms part of the signalling used for service following, which is described in detail in ETSI TS 103 176
 *
 * The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the SId field.
 *
 * The Change Event Indication (CEI) is signalled by the Number of EIds field = 0.
 *
 * The repetition rates for FIG 0/24 are provided in ETSI TS 103 176.
 */
class Fig_00_Ext_24 : public Fig_00 {

public:
    struct OtherEnsembleServiceInformation {
        /*
         * SId (Service Identifier): this 16-bit or 32-bit field shall identify a service.
         * The coding details are given in clause 6.3.1
         */
        uint32_t serviceId{0};

        /*
         * CAId (Conditional Access Identifier): this 3-bit field shall identify the Access Control System (ACS) used for the
         * service. The definition is given in ETSI TS 102 367 [4]. A non-CA capable DAB receiver shall not interpret this field.
         * If no ACS is used for the service, CAId is set to zero.
         */
        uint8_t caId{0x00};

        /*
         * List of EIds (Ensemble identifiers)
         */
        std::vector<uint16_t> ensembleIds;

        /*
         * The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the SId field.
         */
        uint32_t oeDbKey{0};

        /*
         * If the service is carried in the tuned ensemble the OE flag shall be set to "0"
         * If the service is not carried in the tuned ensemble the OE flag shall be set to "1".
         */
        bool isOtherEnsemble{false};

        /*
         * The Change Event Indication (CEI) is signalled by the Number of EIds field = 0.
         */
        bool isChangeEvent{false};

        inline bool operator==(const OtherEnsembleServiceInformation &other) const {
            return serviceId == other.serviceId &&
                   caId == other.caId &&
                   oeDbKey == other.oeDbKey &&
                   isOtherEnsemble == other.isOtherEnsemble &&
                   isChangeEvent == other.isChangeEvent &&
                   containsAllEnsembleIds(other.ensembleIds);
        }

        inline bool operator!=(const OtherEnsembleServiceInformation &other) const { { return !operator==(other); } }

        inline bool containsAllEnsembleIds(const std::vector<uint16_t> &other) const {
            if (ensembleIds.size() == other.size()) {
                for (const auto otherEnsembledId : other) {
                    if (std::find(ensembleIds.cbegin(), ensembleIds.cend(), otherEnsembledId) ==
                            ensembleIds.cend()) {
                        return false;
                    }
                }
                return true;
            }
            return true;
        }
    };

public:
    explicit Fig_00_Ext_24(const std::vector<uint8_t>& figData);
    virtual ~Fig_00_Ext_24();

    const std::vector<OtherEnsembleServiceInformation>& getOtherEnsembleServiceInformations() const { return m_oeSrvInfos; };

private:
    void parseFigData(const std::vector<uint8_t>& figData);

private:
    const std::string m_logTag = {"[Fig_00_Ext_24]"};

    std::vector<OtherEnsembleServiceInformation> m_oeSrvInfos;
};

#endif // FIG_00_EXT_24_H
