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

#include "../fig_00_ext_06.h"


void testCEI() {
    const std::vector <uint8_t> fig = {
            0x86, 0x41, 0x90, 0x41, 0x91, 0x41, 0x93, 0x41,
            0x94, 0x41, 0x97, 0x61, 0x90, 0x61, 0x91, 0x61,
            0x93, 0x61, 0x94, 0x61, 0x97
    };
/*
   8   6   4   1   9   0   4   1   9   1   4   1   9   3   4   1   9   4   4   1   9   7   6   1   9   0   6   1   9   1   6   1   9   3   6   1   9   4   6   1   9   7
100001100100000110010000010000011001000101000001100100110100000110010100010000011001011101100001100100000110000110010001011000011001001101100001100101000110000110010111
1 C/N
 0 OE
  0 P/D
   00110 Ext 6
-------- start of service linking array index 0
        0 Id (Identifier) list flag: Id list and the preceding byte absent
         1 LA (Linkage Actuator): active link
          0 S/H Soft/Hard: Soft link
           0 ILS (International linkage set indicator): national link
            000110010000 LSN (Linkage Set Number) 12-bit: 0x190                  -> Database key: 0(=O/E)0(=P/D)0(=S/H)0(=ILS)000110010000(=ILS) -> 0000000110010000 = 0x190
-------- start of service linking array index 1
                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                         1 LA (Linkage Actuator): active link
                          0 S/H Soft/Hard: Soft link
                           0 ILS (International linkage set indicator): national link
                            000110010001 LSN (Linkage Set Number) 12-bit: 0x191     -> Database key: 0(=O/E)0(=P/D)0(=S/H)0(=ILS)000110010000(=ILS) -> 0000000110010001 = 0x191
-------- start of service linking array index 2
                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                         1 LA (Linkage Actuator): active link
                                          0 S/H(Soft/Hard: Soft link
                                           0 ILS (International linkage set indicator): national link
                                            000110010011 LSN (Linkage Set Number) 12-bit: 0x193     -> Database key: 0(=O/E)0(=P/D)0(=S/H)0(=ILS)000110010000(=ILS) -> 0000000110010011 = 0x193
-------- start of service linking array index 3
                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                         1 LA (Linkage Actuator): active link;
                                                          0 S/H Soft/Hard: Soft link
                                                           0 ILS (International linkage set indicator): national link
                                                            000110010100 LSN (Linkage Set Number) 12-bit: 0x194  -> Database key: 0(=O/E)0(=P/D)0(=S/H)0(=ILS)000110010000(=ILS) -> 0000000110010100 = 0x194
-------- start of service linking array index 4
                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                         1 LA (Linkage Actuator): active link;
                                                                          0 S/H Soft/Hard: Soft link
                                                                           0 ILS (International linkage set indicator): national link
                                                                            000110010111 LSN (Linkage Set Number) 12-bit: 0x197 -> Database key: 0(=O/E)0(=P/D)0(=S/H)0(=ILS)000110010000(=ILS) -> 0000000110010111 = 0x197
-------- start of service linking array index 5
                                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                                         1 LA (Linkage Actuator): active link;
                                                                                          1 S/H Soft/Hard: Hard link
                                                                                           0 ILS (International linkage set indicator): national link
                                                                                            000110010000 LSN (Linkage Set Number) 12-bit: 0x190  -> Database key: 0(=O/E)0(=P/D)1(=S/H)0(=ILS)000110010000(=ILS) -> 0010000110010000 = 0x2190
-------- start of service linking array index 6
                                                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                                                         1 LA (Linkage Actuator): active link;
                                                                                                          1 S/H Soft/Hard: Hard link
                                                                                                           0 ILS (International linkage set indicator): national link
                                                                                                            000110010001 LSN (Linkage Set Number) 12-bit: 0x191  -> Database key: 0(=O/E)0(=P/D)1(=S/H)0(=ILS)000110010000(=ILS) -> 0010000110010001 = 0x2191
-------- start of service linking array index 7
                                                                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                                                                         1 LA (Linkage Actuator): active link;
                                                                                                                          1 S/H Soft/Hard: Hard link
                                                                                                                           0 ILS (International linkage set indicator): national link
                                                                                                                            000110010011 LSN (Linkage Set Number) 12-bit: 0x193  -> Database key: 0(=O/E)0(=P/D)1(=S/H)0(=ILS)000110010000(=ILS) -> 0010000110010011 = 0x2193
-------- start of service linking array index 8
                                                                                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                                                                                         1 LA (Linkage Actuator): active link;
                                                                                                                                          1 S/H Soft/Hard: Hard link
                                                                                                                                           0 ILS (International linkage set indicator): national link
                                                                                                                                            000110010100 LSN (Linkage Set Number) 12-bit: 0x194  -> Database key: 0(=O/E)0(=P/D)1(=S/H)0(=ILS)000110010000(=ILS) -> 0010000110010100 = 0x2194
-------- start of service linking array index 9
                                                                                                                                                        0 Id (Identifier) list flag: Id list and the preceding byte absent
                                                                                                                                                         1 LA (Linkage Actuator): active link;
                                                                                                                                                          1 S/H Soft/Hard: Hard link
                                                                                                                                                           0 ILS (International linkage set indicator): national link
                                                                                                                                                            000110010111 LSN (Linkage Set Number) 12-bit: 0x197  -> Database key: 0(=O/E)0(=P/D)1(=S/H)0(=ILS)000110010000(=ILS) -> 0010000110010111 = 0x2197
*/
    Fig_00_Ext_06 fig_0_06(fig);

    const auto & serviceLinkingInfo = fig_0_06.getServiceLinkingInformations();
    assert(serviceLinkingInfo.size() == 10);
    auto sli = serviceLinkingInfo[0];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == true);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x190);
    assert(sli.linkDbKey == 0x190);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[1];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == true);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x191);
    assert(sli.linkDbKey == 0x191);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[2];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == true);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x193);
    assert(sli.linkDbKey == 0x193);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[3];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == true);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x194);
    assert(sli.linkDbKey == 0x194);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[4];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == true);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x197);
    assert(sli.linkDbKey == 0x197);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[5];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == false);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x190);
    assert(sli.linkDbKey == 0x2190);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[6];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == false);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x191);
    assert(sli.linkDbKey == 0x2191);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[7];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == false);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x193);
    assert(sli.linkDbKey == 0x2193);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[8];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == false);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x194);
    assert(sli.linkDbKey == 0x2194);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);

    sli = serviceLinkingInfo[9];
    assert(sli.isDataService == false);
    assert(sli.isChangeEvent == true);
    assert(sli.isContinuation == false);
    assert(sli.linkageActive == true);
    assert(sli.isSoftLink == false);
    assert(sli.isIls == false);
    assert(sli.linkageSetNumber == 0x197);
    assert(sli.linkDbKey == 0x2197);
    assert(sli.keyServiceId == 0);
    assert(sli.serviceLinks.size() == 0);
}

int main() {

    testCEI();

    std::cout << "PASS" << std::endl;
    return 0;
}
