package org.omri.radio.impl;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.omri.radioservice.RadioServiceMimeType;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C) 2018 IRT GmbH
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
 * @author Fabian Sattler, IRT GmbH
 */

public class RadioDnsEpgBearerDab extends RadioDnsEpgBearer implements Serializable {

    private static final long serialVersionUID = -4897926993570436519L;

    private final String TAG = "RadioDnsEpgBearerDab";

    private final String mBearerId;
    private final int mCost;
    private final int mBitrate;
    private final String mMimeVal;

    private final int mEnsembleEcc;
    private final int mEnsembleId;
    private final int mServiceId;
    private final int mScid;
    private final int mUaType;
    private final RadioServiceMimeType mMimeType;

    RadioDnsEpgBearerDab(String bearerId, int cost, String mimeVal, int bitrate) {
        if (bearerId != null) {
            mBearerId = bearerId;
        } else {
            Log.w(TAG, "bearerId null");
            mBearerId = "";
        }
        if (mimeVal != null) {
            mMimeVal = mimeVal;
        } else {
            Log.w(TAG, "mimeVal null");
            mMimeVal = "";
        }
        mCost = cost;
        mBitrate = bitrate;

        // ETSI TS 103 270 V1.2.1, 5.1.2.4 Construction of bearerURI
        // The bearerURI for a DAB/DAB+ service is compiled as follows:
        //   dab:<gcc>.<eid>.<sid>.<scids>[.<uatype>]
        // examples:
        // dab:ce1.c185.e1c00098.0.004
        // dab:de0.100c.d220.0

        final String regExNoUaType = "dab:(\\p{XDigit}+)\\.(\\p{XDigit}+)\\.(\\p{XDigit}+)\\.(\\p{XDigit}+)";
        final String regExWithUaType = "dab:(\\p{XDigit}+)\\.(\\p{XDigit}+)\\.(\\p{XDigit}+)\\.(\\p{XDigit}+)\\.(\\p{XDigit}+)";
        final Pattern patternNoUatype = Pattern.compile(regExNoUaType);
        final Pattern patternWithUaType = Pattern.compile(regExWithUaType);

        Matcher m = patternNoUatype.matcher(mBearerId);
        boolean hasMatched = m.matches();
        if (!hasMatched) {
            m = patternWithUaType.matcher(mBearerId);
            hasMatched = m.matches();
            if (!hasMatched) {
                Log.w(TAG, "no valid bearer id:'" + bearerId + "'");
            }
        }

        int ecc = -1;
        if (hasMatched) {
            try {
                ecc = Integer.parseInt(m.group(1), 16);
                ecc = ecc & 0x000000FF; // ecc is last two digits of gcc
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mEnsembleEcc = ecc;

        int eid = -1;
        if (hasMatched) {
            try {
                eid = Integer.parseInt(m.group(2), 16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mEnsembleId = eid;

        int sid = -1;
        if (hasMatched) {
            try {
                sid = (int) Long.parseLong(m.group(3), 16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mServiceId = sid;

        int scids = -1;
        if (hasMatched) {
            try {
                scids = Integer.parseInt(m.group(4), 16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mScid = scids;

        int uatype = -1;
        if (m.pattern().equals(patternWithUaType)) {
            if (hasMatched) {
                try {
                    uatype = Integer.parseInt(m.group(5), 16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        mUaType = uatype;

        if (mMimeVal.equalsIgnoreCase("audio/mpeg")) {
            mMimeType = RadioServiceMimeType.AUDIO_MPEG;
        } else if (mMimeVal.equalsIgnoreCase("audio/aac")) {
            mMimeType = RadioServiceMimeType.AUDIO_AAC;
        } else {
            mMimeType = RadioServiceMimeType.UNKNOWN;
        }
    }

    @Override
    public String getBearerId() {
        return mBearerId;
    }

    @Override
    public RadioDnsEpgBearerType getBearerType() {
        return RadioDnsEpgBearerType.DAB;
    }

    @Override
    public int getCost() {
        return mCost;
    }

    @Override
    public int getBitrate() {
        return mBitrate;
    }

    @Override
    public String getMimeValue() {
        return mMimeVal;
    }

    public RadioServiceMimeType getMimeType() {
        return mMimeType;
    }

    public int getServiceId() {
        return mServiceId;
    }

    public int getEnsembleEcc() {
        return mEnsembleEcc;
    }

    public int getServiceComponentId() {
        return mScid;
    }

    public int getEnsembleId() {
        return mEnsembleId;
    }

    public int getUserApplicationType() {
        return mUaType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null) {
            if(obj instanceof RadioDnsEpgBearerDab) {
                RadioDnsEpgBearerDab compSrv = (RadioDnsEpgBearerDab) obj;
                return ((compSrv.getEnsembleId() == this.mEnsembleId) && compSrv.getBearerId().equals(this.mBearerId) && (compSrv.getServiceId() == this.mServiceId) && (compSrv.getEnsembleEcc() == this.mEnsembleEcc) && (compSrv.getServiceComponentId() == this.mScid));
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(mEnsembleId, mBearerId, mServiceId, mEnsembleEcc, mScid, mCost, mBitrate, mMimeVal, mUaType);
        } else {
            int hash = 2;
            hash = 63 * hash + (int)(this.mEnsembleId ^ (this.mEnsembleId >>> 32));
            hash = 63 * hash + this.mBearerId.hashCode() ^ (this.mBearerId.hashCode() >>> 32);
            hash = 63 * hash + (int)(this.mServiceId ^ (this.mServiceId >>> 32));
            hash = 63 * hash + (int)(this.mEnsembleEcc ^ (this.mEnsembleEcc >>> 32));
            hash = 63 * hash + (int)(this.mScid ^ (this.mScid >>> 32));
            hash = 63 * hash + (int)(this.mCost ^ (this.mCost >>> 32));
            hash = 63 * hash + (int)(this.mBitrate ^ (this.mBitrate >>> 32));
            hash = 63 * hash + this.mMimeVal.hashCode() ^ (this.mMimeVal.hashCode() >>> 32);
            hash = 63 * hash + (int)(this.mUaType ^ (this.mUaType >>> 32));

            return hash;
        }
    }

    @Override
    public int compareTo(@NonNull RadioDnsEpgBearer o) {
        return this.mBearerId.compareTo(o.getBearerId());
    }
}
