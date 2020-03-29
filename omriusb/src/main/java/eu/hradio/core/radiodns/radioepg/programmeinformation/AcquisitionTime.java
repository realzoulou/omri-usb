package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;

public class AcquisitionTime implements Serializable
{
    private static final long serialVersionUID = 133014625357314956L;
    private final TimePoint mStartTime;
    private final TimePoint mEndTime;
    
    public AcquisitionTime(@NonNull final String startTime, @NonNull final String endTime) {
        this.mStartTime = new TimePoint(startTime);
        this.mEndTime = new TimePoint(endTime);
    }
    
    public TimePoint getStartTime() {
        return this.mStartTime;
    }
    
    public TimePoint getEndTime() {
        return this.mEndTime;
    }
}
