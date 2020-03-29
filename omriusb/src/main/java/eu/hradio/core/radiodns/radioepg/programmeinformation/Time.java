package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;
import java.text.*;
import java.util.*;

public class Time implements Serializable
{
    private static final long serialVersionUID = -5494181602001913657L;
    private final TimePoint mStartTime;
    private final Duration mDuration;
    private final TimePoint mActualStartTime;
    private final Duration mActualDuration;
    private final TimePoint mEndTime;
    
    public Time(@NonNull final String startTime, @NonNull final String duration, final String actualStartTime, final String actualDuration) {
        this.mStartTime = new TimePoint(startTime);
        this.mDuration = new Duration(duration);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault());
        final long endTimeMillis = this.mStartTime.getTimePointCalendar().getTimeInMillis() + this.mDuration.getDurationSeconds() * 1000;
        final Date endDate = new Date(endTimeMillis);
        this.mEndTime = new TimePoint(dateFormat.format(endDate));
        if (actualStartTime != null && !actualStartTime.isEmpty()) {
            this.mActualStartTime = new TimePoint(actualStartTime);
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
    
    public Time(@NonNull final String startTime, @NonNull final String duration, final String actualStartTime) {
        this(startTime, duration, actualStartTime, null);
    }
    
    public Time(@NonNull final String startTime, @NonNull final String duration) {
        this(startTime, duration, null, null);
    }
    
    public TimePoint getStartTime() {
        return this.mStartTime;
    }
    
    public Duration getDuration() {
        return this.mDuration;
    }
    
    public TimePoint getEndTime() {
        return this.mEndTime;
    }
    
    public TimePoint getActualStartTime() {
        return this.mActualStartTime;
    }
    
    public Duration getActualDuration() {
        return this.mActualDuration;
    }
}
