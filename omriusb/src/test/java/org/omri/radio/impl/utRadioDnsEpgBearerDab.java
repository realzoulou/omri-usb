package org.omri.radio.impl;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.runners.MockitoJUnitRunner;
import org.omri.radioservice.RadioServiceMimeType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Copyright (C) 2020 realzoulou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author realzoulou
 */

@RunWith(MockitoJUnitRunner.class)

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class utRadioDnsEpgBearerDab {
    @Test
    public void t1_nullInputStrings() {
        final RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                null, -1, null, -1);
        assertEquals("", rdnsEpgBearerDab.getBearerId());
    }
    @Test
    public void t2_emptyInputStrings() {
        final RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "", -1, "", -1);
        assertEquals("", rdnsEpgBearerDab.getBearerId());
    }
    @Test
    public void t3_fmBearer() {
        final RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "fm:ce1.c586.09580", -1, "", -1);
        assertEquals(-1, rdnsEpgBearerDab.getEnsembleEcc());
        // nevertheless DAB !
        assertEquals(RadioDnsEpgBearerType.DAB, rdnsEpgBearerDab.getBearerType());
        assertEquals(RadioServiceMimeType.UNKNOWN, rdnsEpgBearerDab.getMimeType());
    }
    @Test
    public void t4_withoutUaType() {
        RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:de0.100c.d220.0", -1, "audio/mpeg", -1);
        assertEquals(0xe0, rdnsEpgBearerDab.getEnsembleEcc());
        assertEquals(0x100c, rdnsEpgBearerDab.getEnsembleId());
        assertEquals(0xd220, rdnsEpgBearerDab.getServiceId());
        assertEquals(0, rdnsEpgBearerDab.getServiceComponentId());
        assertEquals(-1, rdnsEpgBearerDab.getUserApplicationType());
        assertEquals("audio/mpeg", rdnsEpgBearerDab.getMimeValue());
        assertEquals(RadioServiceMimeType.AUDIO_MPEG, rdnsEpgBearerDab.getMimeType());
        assertEquals(-1, rdnsEpgBearerDab.getBitrate());

        rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:0de0.100c.d220.0", -1, "audio/mpeg", -1);
        assertEquals(0xe0, rdnsEpgBearerDab.getEnsembleEcc());
    }
    @Test
    public void t5_withUaType() {
        final RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.0.004", 80, "audio/aac", 48000);
        assertEquals(0xe1, rdnsEpgBearerDab.getEnsembleEcc());
        assertEquals(0xc185, rdnsEpgBearerDab.getEnsembleId());
        assertEquals(0xe1c00098, rdnsEpgBearerDab.getServiceId());
        assertEquals(0, rdnsEpgBearerDab.getServiceComponentId());
        assertEquals(4, rdnsEpgBearerDab.getUserApplicationType());
        assertEquals(80, rdnsEpgBearerDab.getCost());
        assertEquals("audio/aac", rdnsEpgBearerDab.getMimeValue());
        assertEquals(RadioServiceMimeType.AUDIO_AAC, rdnsEpgBearerDab.getMimeType());
        assertEquals(48000, rdnsEpgBearerDab.getBitrate());
    }

    @Test
    public void t6_compare() {
        final RadioDnsEpgBearerDab rdnsEpgBearerDab1 = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.0.004", 80, "audio/aac", 48000);
        final RadioDnsEpgBearerDab rdnsEpgBearerDab2 = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.0.004", 80, "audio/aac", 48000);
        final RadioDnsEpgBearerDab rdnsEpgBearerDab3 = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.0.005", 80, "audio/aac", 48000);

        assertEquals(rdnsEpgBearerDab1, rdnsEpgBearerDab2);
        assertNotEquals(rdnsEpgBearerDab1, rdnsEpgBearerDab3);
    }

    @Test
    public void t6_parseErrors() {
        RadioDnsEpgBearerDab rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.Z.004", 80, "audio/aac", 48000);
        assertEquals(-1, rdnsEpgBearerDab.getServiceComponentId());

        rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab::ce1.c185.e1c00098.0.004", 80, "audio/aac", 48000);
        assertEquals(-1, rdnsEpgBearerDab.getEnsembleEcc());

        rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.e1c00098.0.", 80, "audio/aac", 48000);
        assertEquals(-1, rdnsEpgBearerDab.getUserApplicationType());
        assertEquals(-1, rdnsEpgBearerDab.getEnsembleEcc());

        rdnsEpgBearerDab = new RadioDnsEpgBearerDab(
                "dab:ce1.c185.zucker.0.4", 80, "audio/aac", 48000);
        assertEquals(-1, rdnsEpgBearerDab.getServiceComponentId());
    }
}
