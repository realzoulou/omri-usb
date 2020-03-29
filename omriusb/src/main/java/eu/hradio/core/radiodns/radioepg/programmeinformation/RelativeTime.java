package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;

public class RelativeTime implements Serializable
{
    private static final long serialVersionUID = 8444950190550829120L;
    private final Duration mStartTime;
    private final Duration mDuration;
    private final Duration mActualStartTime;
    private final Duration mActualDuration;
    
    public RelativeTime(@NonNull final String startTime, @NonNull final String duration, final String actualStartTime, final String actualDuration) {
        this.mStartTime = new Duration(startTime);
        this.mDuration = new Duration(duration);
        if (actualStartTime != null && !actualStartTime.isEmpty()) {
            this.mActualStartTime = new Duration(actualStartTime);
        }
        else {
            this.mActualStartTime = null;
        }
        if (actualDuration != null && !actualDuration.isEmpty()) {
            this.mActualDuration = new Duration(actualDuration);
        }
        else {
            this.mActualDuration = null;
        }
    }
    
    public RelativeTime(@NonNull final String startTime, @NonNull final String duration, final String actualStartTime) {
        this(startTime, duration, actualStartTime, null);
    }
    
    public RelativeTime(@NonNull final String startTime, @NonNull final String duration) {
        this(startTime, duration, null, null);
    }
    
    public Duration getStartTime() {
        return this.mStartTime;
    }
    
    public Duration getDuration() {
        return this.mDuration;
    }
    
    public Duration getActualStartTime() {
        return this.mActualStartTime;
    }
    
    public Duration getActualDuration() {
        return this.mActualDuration;
    }
}
