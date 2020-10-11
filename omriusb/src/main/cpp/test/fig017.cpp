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


#include <vector>
#include <iostream>

#include <assert.h>

#include "../fig_00_ext_17.h"

int main() {
/* complete FIB:
      01      02      03      04      05      06      07   ...
-------------------------------------------------------|
   0   6   1   1   F   2   1   5   2   0   1   E   0   0   060DFF1A010040FF000000000000000000000000000000B0A0
000 FIG Type=0
   00110 Length=6
        0 C/N
         0 OE
          0 P/D
           10001 Ext=17         |---- v1.4.1 ----------|
                   F   2   1   5 SId=F215
                                0 S/D=Programme Type codes and language (when present), may not represent the current programme contents
                                 0 P/S=primary service component
                                  1 L flag=language field present
                                   0 CC flag=complementary code and preceding Rfa and Rfu fields absent;
                                    0000 Rfa
                                        00011110 Language=30
                                                00 Rfa
                                                  0 Rfu
                                                   00000 Int code=0
 */
    const std::vector<uint8_t> fib_v141 = {
        /*0x06,*/ 0x11, 0xF2, 0x15, 0x20, 0x1E, 0x00 // only the 1st FIG from the FIB
    };

    Fig_00_Ext_17 fig_v141(fib_v141);
    const std::vector<Fig_00_Ext_17::ProgrammeTypeInformation> ptyVec = fig_v141.getProgrammeTypeInformations();
    assert(ptyVec.size() == 1);
    assert(ptyVec[0].serviceId == 0xF215);
    assert(!ptyVec[0].isDynamic);
    assert(ptyVec[0].intPtyCode == 0);
    std::cout << "PASS" << std::endl;
    return 0;
}
