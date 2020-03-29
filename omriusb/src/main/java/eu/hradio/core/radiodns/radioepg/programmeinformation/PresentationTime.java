package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;

public class PresentationTime implements Serializable
{
    private static final long serialVersionUID = 8918595064635296812L;
    private final Duration mDuration;
    private TimePoint mStartTime;
    private TimePoint mEndTime;
    
    public PresentationTime(@NonNull final Duration duration) {
        this.mDuration = duration;
    }
    
    public PresentationTime(@NonNull final String duration) {
        this.mDuration = new Duration(duration);
    }
    
    public void setStartTime(@NonNull final TimePoint timepoint) {
        this.mStartTime = timepoint;
    }
    
    public void setStartTime(@NonNull final String timepoint) {
        this.mStartTime = new TimePoint(timepoint);
    }
    
    public void setEndTime(@NonNull final TimePoint timepoint) {
        this.mEndTime = timepoint;
    }
    
    public void setEndTime(@NonNull final String timepoint) {
        this.mEndTime = new TimePoint(timepoint);
    }
    
    public Duration getDuration() {
        return this.mDuration;
    }
    
    public TimePoint getStartTime() {
        return this.mStartTime;
    }
    
    public TimePoint getEndTime() {
        return this.mEndTime;
    }
}
