/*
 * Copyright (C) 2020 realzoulou
 *
 * Author:
 *  realzoulou
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

#include <cstdint>
#include <vector>
#include <iostream>

#include <cassert>

#include "../fig_00_ext_21.h"

void testFmRds() {
    const std::vector<uint8_t> fig01 = {
            0x15, 0x00, 0x05, 0x71, 0x11, 0x82, 0x3A, 0x5F
    };
/*
      01      02      03      04      05      06      07      08
----------------------------------------------------------------
   1   5   0   0   0   5   7   1   1   1   8   2   3   A   5   F
0001010100000000000001010111000100010001100000100011101001011111
000
0 C/N
 0 OE
  0 P/D
   10101 Ext=21
        00000000000 Rfa
                   00101 Length of FI list = 5 bytes
                        0111000100010001 Id field = 0x7111
                                        1000 R&M = 8 = RDS PI-code
                                            0 Continuity flag = false
                                             010 Length of Freq. list = 2 bytes
                                                00111010 = 58 => 87,5 MHz + (58 × 100 kHz) = 93300 kHz
                                                        01011111 = 95 => 97000 kHz

  // The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field
    00000000000001110001000100011000 = 0x71118
    0 OE
    0 P/D
     00000000000 Rfa
                0111000100010001 Id field
                                1000 R&M = 8 = RDS PI-code
*/

    Fig_00_Ext_21 fig_0_21(fig01);
    assert(fig_0_21.isDataService() == false);
    const auto & freqInfo = fig_0_21.getFrequencyInformations();
    assert(freqInfo.size() == 1);
    const auto & fi = freqInfo[0];
    assert(fi.isContinuation == false);
    assert(fi.isOtherEnsemble == false);
    assert(fi.continuousOutput == false);
    assert(fi.isChangeEvent == false);
    assert(fi.id == 0x7111);
    assert(fi.frequencyInformationType == Fig_00_Ext_21::FM_RDS);
    assert(fi.frequencies.size() == 2);
    assert(fi.frequencies[0].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_UNKNOWN);
    assert(fi.frequencies[0].frequencyKHz == 93300);
    assert(fi.frequencies[1].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_UNKNOWN);
    assert(fi.frequencies[1].frequencyKHz == 97000);
    std::vector<Fig_00_Ext_21::FrequencyListItem> other;
    other.push_back(fi.frequencies[1]);
    other.push_back(fi.frequencies[0]);
    assert(fi.containsAllFreqs(other) == true); // order should not matter!

    uint32_t expFreqDbKey = 0x00071118;
    if (expFreqDbKey != fi.freqDbKey) {
        std::cerr << "freqDbKey was 0x" << std::hex << +fi.freqDbKey
                  << ", expected 0x" << +expFreqDbKey << std::dec << std::endl;
    }
    assert(fi.freqDbKey == expFreqDbKey);
}

void testDab() {
    const std::vector<uint8_t> fig02 = {
            0x15, 0x00, 0x12, 0x11, 0xF7, 0x0E, 0x10, 0x2B, 0xF6, 0x10, 0x30, 0xAC, 0x11, 0xF7, 0x0E, 0x10, 0x31, 0xF6, 0x10, 0x37, 0xED
    };
/*
   1   5   0   0   1   2   1   1   F   7   0   E   1   0   2   B   F   6   1   0   3   0   A   C   1   1   F   7   0   E   1   0   3   1   F   6   1   0   3   7   E   D
000101010000000000010010000100011111011100001110000100000010101111110110000100000011000010101100000100011111011100001110000100000011000111110110000100000011011111101101
000
0 C/N
 0 OE
  0 P/D
   10101 Ext=21
        00000000000 Rfa
                   10010 Length of FI list = 18 bytes
                        0001000111110111 Id field = 0x11F7
                                        0000 R&M = 0 = DAB Ensemble
                                            1 Continuity flag = true
                                             110 Length of Freq. list = 6 bytes
                                                00010  Control field => geographically adjacent area, transmission mode I
                                                     0000010101111110110 Freq a = 11254  => 0 Hz + (11254 × 16 kHz) = 180064 kHz = 5D
                                                                        00010 Control field => geographically adjacent area, transmission mode I
                                                                             0000011000010101100 Freq a = 12460 => 199360 kHz = 8C
                                                                                                00010 Control field => geographically adjacent area, transmission mode I
                                                                                                     0011111011100001110 = 128782 => 2060512 kHz ???
                                                                                                                        00010 Control field => geographically adjacent area, transmission mode I
                                                                                                                             0000011000111110110 = 12790 => 204640 kHz = 9B
                                                                                                                                                00010 Control field => geographically adjacent area, transmission mode I
                                                                                                                                                     0000011011111101101 = 14317 => 229072 kHz = 12D
  // The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field
    00000000000000010001111101110000 = 0x11F70
    0 OE
    0 P/D
     00000000000 Rfa
                0001000111110111 Id field = 0x11F7
                                0000 R&M = 0 = DAB Ensemble
*/


    Fig_00_Ext_21 fig_0_21(fig02);
    assert(fig_0_21.isDataService() == false);
    const auto & freqInfo = fig_0_21.getFrequencyInformations();
    assert(freqInfo.size() == 1);
    const auto & fi = freqInfo[0];
    assert(fi.isContinuation == false);
    assert(fi.isOtherEnsemble == false);
    assert(fi.continuousOutput == true);
    assert(fi.isChangeEvent == false);
    assert(fi.id == 0x11F7);
    assert(fi.frequencyInformationType == Fig_00_Ext_21::DAB_ENSEMBLE);
    assert(fi.frequencies.size() == 4);
    assert(fi.frequencies[0].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE);
    assert(fi.frequencies[0].frequencyKHz == 180064);
    assert(fi.frequencies[1].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE);
    assert(fi.frequencies[1].frequencyKHz == 199360);
    assert(fi.frequencies[2].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE);
    assert(fi.frequencies[2].frequencyKHz == 204640);
    assert(fi.frequencies[3].additionalInfo.dabEnsembleAdjacent == Fig_00_Ext_21::GEOGRAPHICALLY_ADJACENT_TRANSMISSION_MODE_ONE);
    assert(fi.frequencies[3].frequencyKHz == 229072);
    uint32_t expFreqDbKey = 0x00011F70;
    if (expFreqDbKey != fi.freqDbKey){
        std::cerr << "freqDbKey was 0x" << std::hex << +fi.freqDbKey
                  << ", expected 0x" << +expFreqDbKey << std::dec << std::endl;
    }
    assert(fi.freqDbKey == expFreqDbKey);
}

void testDRM() {
    const std::vector <uint8_t> fig = {
            0x15, 0x00, 0x17, 0x11, 0xF7, 0x63, 0xDE, 0xBE, 0xEF
    };

/* Following is an artificial, manually compiled message!

   1   5   0   0   1   7   1   1   F   7   6   3   D   E   B   E   E   F
000101010000000000010111000100011111011101100011110111101011111011101111
000
0 C/N
 0 OE
  0 P/D
   10101 Ext=21
        00000000000 Rfa
                   10111 Length of FI list = 24 bytes
                        0001000111110111 Id field = 0x11F7
                                        0110 R&M = 0 = DRM
                                            0 Continuity flag = false
                                             011 Length of Freq. list = 3 bytes
                                                11011110 Id field 2 = 0xDE
                                                        1 Multiplier => multiply with 10 kHz
                                                          011111011101111 Frequency = 16111 x 10 kHz => 161110 kHz

  // The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field
    00000000000000010001111101110000 = 0x11F70
    0 OE
    0 P/D
     00000000000 Rfa
                0001000111110111 Id field = 0x11F7
                                0110 R&M = 0 = DRM
*/
    Fig_00_Ext_21 fig_0_21(fig);
    assert(fig_0_21.isDataService() == false);
    const auto &freqInfo = fig_0_21.getFrequencyInformations();
    assert(freqInfo.size() == 1);
    const auto &fi = freqInfo[0];
    assert(fi.isContinuation == false);
    assert(fi.isOtherEnsemble == false);
    assert(fi.continuousOutput == false);
    assert(fi.isChangeEvent == false);
    assert(fi.id == 0x11F7);
    assert(fi.frequencyInformationType == Fig_00_Ext_21::DRM);
    assert(fi.frequencies.size() == 1);
    assert(fi.frequencies[0].additionalInfo.serviceIdentifierDrmAmss == 0xDE);
    if (fi.frequencies[0].frequencyKHz != 161110) {
        std::cerr << "frequencyKHz was " << +fi.frequencies[0].frequencyKHz << std::endl;
    }
    assert(fi.frequencies[0].frequencyKHz == 161110);
    uint32_t expFreqDbKey = 0x00011F76;
    if (expFreqDbKey != fi.freqDbKey) {
        std::cerr << "freqDbKey was 0x" << std::hex << +fi.freqDbKey
                  << ", expected 0x" << +expFreqDbKey << std::dec << std::endl;
    }
    assert(fi.freqDbKey == expFreqDbKey);
}

void testCEI() {
    const std::vector <uint8_t> fig = {
            0x15, 0x00, 0x0C, 0x11, 0xF7, 0x00
    };
/*
   1   5   0   0   0   C   1   1   F   7   0   0
000101010000000000001100000100011111011100000000
000
0 C/N
 0 OE
  0 P/D
   10101 Ext=21
        00000000000 Rfa
                   01100 Length of FI list = 12 bytes
                        0001000111110111 Id field = 0x11F7
                                        0000 R&M = 0 = DAB Ensemble
                                            0 Continuity flag = false
                                             000 Length of Freq. list = 0 bytes => Change Event Indication CEI

  // The database key comprises the OE and P/D flags (see clause 5.2.2.1) and the Rfa, Id field, and R&M field
    00000000000000010001111101110000 = 0x11F70
    0 OE
    0 P/D
     00000000000 Rfa
                0001000111110111 Id field = 0x11F7
                                0000 R&M = 0 = DAB Ensemble

*/
    Fig_00_Ext_21 fig_0_21(fig);
    assert(fig_0_21.isDataService() == false);
    const auto &freqInfo = fig_0_21.getFrequencyInformations();
    assert(freqInfo.size() == 1);
    const auto &fi = freqInfo[0];
    assert(fi.isContinuation == false);
    assert(fi.isOtherEnsemble == false);
    assert(fi.continuousOutput == false);
    assert(fi.isChangeEvent == true);
    assert(fi.id == 0x11F7);
    assert(fi.frequencyInformationType == Fig_00_Ext_21::DAB_ENSEMBLE);
    assert(fi.frequencies.size() == 0);
    uint32_t expFreqDbKey = 0x00011F70;
    if (expFreqDbKey != fi.freqDbKey) {
        std::cerr << "freqDbKey was 0x" << std::hex << +fi.freqDbKey
                  << ", expected 0x" << +expFreqDbKey << std::dec << std::endl;
    }
    assert(fi.freqDbKey == expFreqDbKey);
}

int main() {

    testFmRds();

    testDab();

    testDRM();

    testCEI();

    std::cout << "PASS" << std::endl;
    return 0;
}
