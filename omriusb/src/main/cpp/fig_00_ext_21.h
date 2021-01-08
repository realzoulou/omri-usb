/*
 * Copyright (C) 2018 IRT GmbH
 *
 * Author:
 * Fabian Sattler
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

#ifndef FIG_00_EXT_21_H
#define FIG_00_EXT_21_H

#include <algorithm>

#include "fig_00.h"

/*
 * ETS 300 401 clause 8.1.8 Frequency Information
 *
 * The Frequency Information feature is assigned to providing radio Frequency Information (FI).
 * The feature is encoded in Extension 21 of FIG type 0 (FIG 0/21).
 *
 * When the FI applies to the tuned ensemble, or to a Primary service component from the tuned ensemble carried via FM
 * with RDS, AM with AMSS or DRM, the OE flag shall be set to "0".
 *
 * When the FI applies to other ensembles, or to services not in the tuned ensemble carried via FM with RDS, AM with
 * AMSS or DRM, the OE flag shall be set to "1".
 *
 * This feature shall use the SIV signalling (see clause 5.2.2.1). The database shall be divided by use of a database key.
 * Changes to the database shall be signalled using the CEI.
 *
 * Frequency information forms part of the signalling used for service following and OE announcements, which is described in detail in ETSI TS 103 176.
 *
 * The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M fields.
 * The Change Event Indication (CEI) is signalled by the Length of Freq list field = 0.
 *
 * The repetition rates for FIG 0/21 are provided in ETSI TS 103 176.
 */
class Fig_00_Ext_21 : public Fig_00 {

public:
    enum FrequencyInformationType {
        FREQUENCY_INFORMATIONTYPE_UNKNOWN,
        DAB_ENSEMBLE,
        DRM,
        FM_RDS,
        AMSS
    };

    enum DabEnsembleAdjacent {
        GEOGRAPHICALLY_ADJACENT_UNKNOWN = 0,
        GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED,
        GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE,
        GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED,
        GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_ONE
    };

    union FrequencyListItemAdditionalInfo {
        /*
         * Note: dabEnsembleAdjacent is only valid for frequencyInformationType = DAB_ENSEMBLE,
         */
        DabEnsembleAdjacent dabEnsembleAdjacent;

        /*
         * Id field 2: this 8-bit field represents the AMSS Service Identifier (most significant byte) (see ETSI TS 102 386)
         *   or
         * Id field 2: this 8-bit field represents the DRM Service Identifier (most significant byte) (see ETSI ES 201 980)
         */
        uint8_t serviceIdentifierDrmAmss{0};
    };

    struct FrequencyListItem {

        uint32_t frequencyKHz{0};

        FrequencyListItemAdditionalInfo additionalInfo;

        inline bool operator==(const FrequencyListItem& other) const {
            bool ret = true;
            ret &= other.frequencyKHz == frequencyKHz;
            ret &= other.additionalInfo.dabEnsembleAdjacent == additionalInfo.dabEnsembleAdjacent;
            // test also the other union type
            ret &= other.additionalInfo.serviceIdentifierDrmAmss == additionalInfo.serviceIdentifierDrmAmss;
            return ret;
        }

        inline bool operator!=(const FrequencyListItem& other) const { return !operator==(other); }

        inline bool operator<(const FrequencyListItem& other) {
            return other.frequencyKHz < frequencyKHz;
        }
    };

    struct FrequencyInformation {

        /*
         * List of frequencies
         */
        std::vector<FrequencyListItem> frequencies;

        /* frequencyInformationType is derived from R&M */
        FrequencyInformationType frequencyInformationType{FrequencyInformationType::FREQUENCY_INFORMATIONTYPE_UNKNOWN};

        /*
         * Id field (Identifier field): this 16-bit field shall depend on the following R&M field, as follows:
         * - If R&M = 0000 Id field = DAB EId (see clause 6.4)
         * - If R&M = 0110 Id field = DRM Service Identifier (two least significant bytes) (see ETSI ES 201 980)
         * - If R&M = 1000 Id field = RDS PI-code (see IEC 62106 [10])
         * - If R&M = 1110 Id field = AMSS Service Identifier (two least significant bytes) (see ETSI TS 102 386)
         */
        uint16_t id{0};

        /*
         * If R&M = "0000" the continuity flag shall signal that:
         *  0: continuous output not expected;
         *  1: continuous output possible.
         *
         * If R&M = "0110", "1000" or "1110" the continuity flag shall indicate whether, or not, there is an appropriate time delay
         * on the audio signal of an alternative service source on DRM/FM/AM to compensate the decoding time delay of DAB:
         *  0: no compensating time delay on DRM/FM/AM audio signal;
         *  1: compensating time delay on DRM/FM/AM audio signal.
         *
         * For R&M = "0110", "1000" and "1110", the Continuity flag is only valid for OE = "0". For OE = "1", the
         * bit b3 is reserved for future addition.
         */
        bool continuousOutput{false};

        /*
         * derived from O/E flag in FIG header
         */
        bool isOtherEnsemble{false};

        /* The Change Event Indication (CEI) is signalled by the Length of Freq list field = 0 */
        bool isChangeEvent{false};

        /*
         * derived from C/N flag in FIG header
         */
        bool isContinuation{false};

        /* The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field */
        uint32_t freqDbKey{0};

        // too much code for inlining
        bool operator==(const FrequencyInformation& other) const {
            return other.freqDbKey == freqDbKey &&
                   other.id == id &&
                   other.frequencyInformationType == frequencyInformationType &&
                   other.isOtherEnsemble == isOtherEnsemble &&
                   other.continuousOutput == continuousOutput &&
                   other.isChangeEvent == isChangeEvent &&
                   other.isContinuation == isContinuation &&
                   containsAllFreqs(other.frequencies);
        }

        inline bool operator!=(const FrequencyInformation& other) const { return !operator==(other); }

        // too much code for inlining
        bool containsAllFreqs(const std::vector<FrequencyListItem>& otherFreqList) const {
            if (otherFreqList.size() == frequencies.size()) {
                for (const auto & otherItem : otherFreqList) {
                    if (std::find(frequencies.cbegin(), frequencies.cend(), otherItem) == frequencies.cend()) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };

public:
    explicit Fig_00_Ext_21(const std::vector<uint8_t>& figData);
    virtual ~Fig_00_Ext_21();

    const std::vector<Fig_00_Ext_21::FrequencyInformation>& getFrequencyInformations() const { return m_frequencyInformations; };

private:
    void parseFigData(const std::vector<uint8_t>& figData);

private:
    const std::string m_logTag = {"[Fig_00_Ext_21]"};

    std::vector<FrequencyInformation> m_frequencyInformations;
};

#endif // FIG_00_EXT_21_H
