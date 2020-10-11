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

#include "fig_00_ext_21.h"

Fig_00_Ext_21::Fig_00_Ext_21(const std::vector<uint8_t>& figData) : Fig_00(figData) {
    parseFigData(figData);
}

Fig_00_Ext_21::~Fig_00_Ext_21() {

}

void Fig_00_Ext_21::parseFigData(const std::vector<uint8_t>& figData) {
    auto figIter = figData.cbegin() +1;
    while(figIter < figData.cend()) {
        uint16_t rfa = static_cast<uint16_t>(((*figIter++ & 0xFF) << 3) | (((*figIter & 0xE0) >> 5) & 0xFF));
        uint8_t lenFiListBytes = static_cast<uint8_t>((*figIter++ & 0x1F) & 0xFF);

        if (lenFiListBytes == 0) {
            return;
        }
        while (figIter < figData.cend()) {

            auto idField = static_cast<uint16_t>(((*figIter++ & 0xFF) << 8) | (*figIter++ & 0xFF));
            auto rangeModulation =  static_cast<uint8_t>((((*figIter & 0xF0) >> 4) & 0xFF));
            bool contFlag = (((*figIter & 0x08) >> 3) & 0xFF) != 0;
            auto frqListEntryLen = static_cast<uint8_t>(((*figIter++ & 0x07) & 0xFF));

            FrequencyInformation freqInfo;
            freqInfo.id = idField;
            freqInfo.continuousOutput = contFlag;
            freqInfo.isOtherEnsemble = isOtherEnsemble();
            freqInfo.frequencyInformationType =
                    (rangeModulation == 0b0000) ? Fig_00_Ext_21::FrequencyInformationType::DAB_ENSEMBLE :
                    (rangeModulation == 0b0110) ? Fig_00_Ext_21::FrequencyInformationType::DRM:
                    (rangeModulation == 0b1000) ? Fig_00_Ext_21::FrequencyInformationType::FM_RDS:
                    (rangeModulation == 0b1110) ? Fig_00_Ext_21::FrequencyInformationType::AMSS:
                    Fig_00_Ext_21::FrequencyInformationType::FREQUENCY_INFORMATIONTYPE_UNKNOWN;
            /* The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field */
            freqInfo.freqDbKey = static_cast<uint32_t>(
                     (((isOtherEnsemble() && (isDataService())) & 0b1) << 31)  // 1 bit
                    | ((rfa & 0b11111111111) << 20)  // 11 bit
                    | ((idField & 0b1111111111111111) << 4)  // 16 bit
                    | (rangeModulation & 0b1111)  // 4 bit
            );
            bool isDbStart{false};
            if(frqListEntryLen > 0) {
                if(!isNextConfiguration()) {
                    //Start of Database
                    isDbStart = true;
                } else {
                    freqInfo.isContinuation = true;
                }
            } else {
                //Database Change Event Indication (CEI)
                freqInfo.isChangeEvent = true;
            }

            while (frqListEntryLen > 0 && figIter < figData.cend()) {

                FrequencyListItem freqInfoItem;

                //R&M = 0000: DAB Ensemble
                if (rangeModulation == 0) {

                    /*
                     * Control field: this 5-bit field shall be used to qualify the immediately following Freq (Frequency) a field.
                     * The following functions are defined (the remainder shall be reserved for future use of the Freq a field):
                     *  b23 - b19;
                     *  0 0 0 0 0: geographically adjacent area, no transmission mode signalled;
                     *  0 0 0 1 0: geographically adjacent area, transmission mode I;
                     *  0 0 0 0 1: not geographically adjacent area, no transmission mode signalled;
                     *  0 0 0 1 1: not geographically adjacent area, transmission mode I.
                     */
                    auto controlField = static_cast<uint8_t>((((*figIter & 0xF8) >> 3) & 0xFF));

                    switch (controlField) {
                        case 0b00000: // geographically adjacent area, no transmission mode signalled
                            freqInfoItem.additionalInfo.dabEnsembleAdjacent = DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED;
                            break;
                        case 0b00010: // geographically adjacent area, transmission mode I
                            freqInfoItem.additionalInfo.dabEnsembleAdjacent = DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE;
                            break;
                        case 0b00001: // not geographically adjacent area, no transmission mode signalled
                            freqInfoItem.additionalInfo.dabEnsembleAdjacent = DabEnsembleAdjacent::GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_NOT_SIGNALLED;
                            break;
                        case 0b00011: // not geographically adjacent area, transmission mode I
                            freqInfoItem.additionalInfo.dabEnsembleAdjacent = DabEnsembleAdjacent::GEOGRAPHICALLY_NOT_ADJACENT_TRANSMISSION_MODE_ONE;
                            break;
                        default: // the remainder shall be reserved for future use of the Freq a field
                            freqInfoItem.additionalInfo.dabEnsembleAdjacent = DabEnsembleAdjacent::GEOGRAPHICALLY_ADJACENT_UNKNOWN;
                            break;
                    };
                    /*
                     * Freq (Frequency) a: this 19-bit field, coded as an unsigned binary number, shall represent the
                     * carrier frequency associated with the alternative service source or other service.
                     *
                     * The centre carrier frequency of the other ensemble is given by (in this expression, the decimal equivalent of freqInfoItem a is used):
                     *  0 Hz + (Freq a × 16 kHz).
                     *
                     * The following values of the carrier frequency are defined:
                     *  b18                                  b0    Decimal
                     *   0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0       0       : Not to be used;
                     *   0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1       1       : 16 kHz;
                     *   0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0       2       : 32 kHz;
                     *  ..............................................................
                     *   1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1    524 287    : 8 388 592 kHz.
                     */
                    uint32_t frequency = static_cast<uint32_t>(
                            ((*figIter++ & 0x07) << 16) | ((*figIter++ & 0xFF) << 8) |
                            ((*figIter++ & 0xFF)));

                    freqInfoItem.frequencyKHz = frequency * 16;
                } else

                    //R&M = 1000: FM with RDS
                if (rangeModulation == 8) {
                    /*
                     * Freq (Frequency) b: this 8-bit field, coded as an unsigned binary number, shall represent the carrier frequency associated with the other service:
                     *
                     * The carrier frequency of the FM transmission is given by (in this expression, the decimal equivalent of freqInfoItem b is used):
                     *  87,5 MHz + (Freq b × 100 kHz).
                     *
                     * The following values of the carrier frequency are defined (other values shall be reserved for future use):
                     *
                     * b7           b0   Decimal
                     * 0 0 0 0 0 0 0 0      0       : Not to be used;
                     * 0 0 0 0 0 0 0 1      1       : 87,6 MHz;
                     * 0 0 0 0 0 0 0 1      1       : 87,7 MHz;
                     * ...............................................
                     * 1 1 0 0 1 1 0 0     204      : 107,9 MHz.
                     */
                    uint8_t frequency = static_cast<uint8_t>((*figIter++ & 0xFF));
                    //std::cout << m_logTag << " FM Frequency " << +frequency << " : "
                    //          << +(frequency * 100 + 87500) << " kHz" << std::endl;
                    freqInfoItem.frequencyKHz = frequency * 100 + 87500;
                    freqInfoItem.additionalInfo.dabEnsembleAdjacent = GEOGRAPHICALLY_ADJACENT_UNKNOWN;
                    freqInfoItem.additionalInfo.serviceIdentifierDrmAmss = 0;
                } else

                    //R&M = 0110: DRM
                    //R&M = 1110: AMSS
                if (rangeModulation == 14 || rangeModulation == 6) {
                    /*
                     * Id field 2: this 8-bit field represents the AMSS Service Identifier (most significant byte) (see ETSI TS 102 386)
                     */
                    /*
                     *  Id field 2: this 8-bit field represents the DRM Service Identifier (most significant byte) (see ETSI ES 201 980).
                     */
                    uint8_t idField2 = static_cast<uint8_t>(*figIter++ & 0xFF);
                    freqInfoItem.additionalInfo.serviceIdentifierDrmAmss = idField2;

                    /*
                     *  Freq (Frequency) c: this 16-bit field, consists of the following fields:
                     */

                    /*
                     * Rfu: this 1 bit field shall be reserved for future use of the frequency field and shall be set to zero until defined.
                     */
                    bool rfu = (((*figIter & 0x80) >> 7) & 0xFF) != 0;

                    /*
                     * frequency: this 15-bit field, coded as an unsigned binary number, shall represent the centre frequency associated with the other service in kHz
                     *
                     * The following values of the reference frequency are defined:
                     *
                     * b14                        b0  Decimal
                     * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0     0      : Not to be used;
                     * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1     1      : 1 kHz;
                     * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1     1      : 1 kHz;
                     * ........................................................
                     * 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1   32 767   : 32 767 kHz.
                     */
                    bool multiplier = *figIter >> 7U != 0;
                    uint16_t frequency = static_cast<uint8_t>(((*figIter++ & 0x7F) << 8) |
                                                              ((*figIter++ & 0xFF)));
                    freqInfoItem.frequencyKHz = (multiplier ? frequency * 10 : frequency);
                } else {
                    // ETSI 300 401 v1.4.1 still had
                    // rangeModulation = 10
                    // 1 0 1 0:    AM (MW in 9 kHz steps & LW)
                    // rangeModulation = 12
                    // 1 1 0 0:    AM (MW in 5 kHz steps & SW);

                    // ignored
                    break;
                }

                freqInfo.frequencies.push_back(freqInfoItem);
            }

            m_frequencyInformations.push_back(freqInfo);
        }
    }
}
