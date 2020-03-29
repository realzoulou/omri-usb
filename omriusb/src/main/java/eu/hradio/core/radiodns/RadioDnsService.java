package eu.hradio.core.radiodns;

import org.omri.radioservice.*;
import org.minidns.record.*;
import android.os.*;
import java.util.*;

public abstract class RadioDnsService
{
    private static final String TAG = "RadioDnsService";
    private final String mTarget;
    private final int mPort;
    private final int mWeight;
    private final int mPriority;
    private final long mTimeToLive;
    private final RadioDnsServiceType mSrvType;
    private final String mSrvId;
    private final String mBearerUri;
    private final RadioService mLookupSrv;
    
    RadioDnsService(final SRV srvRecord, final String rdnsSrvId, final String bearerUri, final RadioDnsServiceType srvType, final RadioService loopupSrv) {
        String target = srvRecord.target.toString();
        if (target.endsWith(".")) {
            target = target.substring(0, target.length() - 1);
        }
        this.mTarget = target;
        this.mPort = srvRecord.port;
        this.mWeight = srvRecord.weight;
        this.mPriority = srvRecord.priority;
        this.mTimeToLive = 3600L;
        this.mSrvType = srvType;
        this.mSrvId = rdnsSrvId;
        this.mBearerUri = bearerUri;
        this.mLookupSrv = loopupSrv;
    }
    
    public String getTarget() {
        return this.mTarget;
    }
    
    public int getPort() {
        return this.mPort;
    }
    
    public int getWeight() {
        return this.mWeight;
    }
    
    public int getPriority() {
        return this.mPriority;
    }
    
    public RadioDnsServiceType getServiceType() {
        return this.mSrvType;
    }
    
    public String getServiceIdentifier() {
        return this.mSrvId;
    }
    
    public String getBearerUri() {
        return this.mBearerUri;
    }
    
    public long getTtl() {
        return this.mTimeToLive;
    }
    
    public RadioService getRadioService() {
        return this.mLookupSrv;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RadioDnsService) {
            final RadioDnsService compSrv = (RadioDnsService)obj;
            return compSrv.getTarget().equals(this.mTarget) && compSrv.getServiceType() == this.mSrvType && compSrv.getPort() == this.mPort && compSrv.getWeight() == this.mWeight && compSrv.getPriority() == this.mPriority;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.hash(this.mTarget, this.mSrvType, this.mPort, this.mWeight, this.mPriority);
        }
        int hash = 31;
        hash = 67 * hash + this.mTarget.hashCode();
        hash = 67 * hash + this.mSrvType.hashCode();
        hash = 67 * hash + (this.mPort ^ this.mPort >>> 32);
        hash = 67 * hash + (this.mWeight ^ this.mWeight >>> 32);
        hash = 67 * hash + (this.mPriority ^ this.mPriority >>> 32);
        return hash;
    }
}
