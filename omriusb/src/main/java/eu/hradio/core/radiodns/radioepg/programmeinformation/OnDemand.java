package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import androidx.annotation.NonNull;
import java.util.*;

public class OnDemand implements Serializable
{
    private static final long serialVersionUID = 1008687064185484577L;
    private final PresentationTime mPresentationTime;
    private List<Bearer> mBearers;
    private List<AcquisitionTime> mAcquisitionTimes;
    
    public OnDemand(@NonNull final PresentationTime preTime, @NonNull final Bearer bearer) {
        this.mBearers = new ArrayList<Bearer>();
        this.mAcquisitionTimes = new ArrayList<AcquisitionTime>();
        this.mPresentationTime = preTime;
        this.mBearers.add(bearer);
    }
    
    public OnDemand(@NonNull final PresentationTime preTime, @NonNull final List<Bearer> bearers) {
        this.mBearers = new ArrayList<Bearer>();
        this.mAcquisitionTimes = new ArrayList<AcquisitionTime>();
        this.mPresentationTime = preTime;
        this.mBearers.addAll(bearers);
    }
    
    public void addAcquisitionTime(final AcquisitionTime acqTime) {
        this.mAcquisitionTimes.add(acqTime);
    }
    
    public void addAcquisitionTimes(final List<AcquisitionTime> acqTimes) {
        this.mAcquisitionTimes.addAll(acqTimes);
    }
    
    public PresentationTime getPresentationTime() {
        return this.mPresentationTime;
    }
    
    public List<Bearer> getBearers() {
        return this.mBearers;
    }
    
    public List<AcquisitionTime> getAcquisitionTimes() {
        return this.mAcquisitionTimes;
    }
}
