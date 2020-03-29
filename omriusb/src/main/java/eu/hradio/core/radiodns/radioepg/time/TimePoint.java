package eu.hradio.core.radiodns.radioepg.time;

import androidx.annotation.NonNull;

import java.io.*;
import java.util.*;
import java.text.*;

public class TimePoint implements Serializable
{
    private static final long serialVersionUID = 9050635464076149559L;
    private final String mTimePoint;
    private TimeZone mTimeOffset;
    private Calendar mTimePointCalendar;
    
    public TimePoint(@NonNull final String timepoint) {
        this.mTimePoint = timepoint.trim();
        if (!this.mTimePoint.isEmpty()) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault());
            try {
                dateFormat.parse(this.mTimePoint.replaceAll("Z$", "+00:00"));
                this.mTimeOffset = dateFormat.getTimeZone();
                this.mTimePointCalendar = dateFormat.getCalendar();
            }
            catch (ParseException e) {
                (this.mTimePointCalendar = Calendar.getInstance()).set(1970, 1, 1, 0, 0, 0);
                this.mTimeOffset = this.mTimePointCalendar.getTimeZone();
            }
        }
        else {
            (this.mTimePointCalendar = Calendar.getInstance()).set(1970, 1, 1, 0, 0, 0);
            this.mTimeOffset = this.mTimePointCalendar.getTimeZone();
        }
    }
    
    public String getTimePoint() {
        return this.mTimePoint;
    }
    
    public TimeZone getTimeOffset() {
        return this.mTimeOffset;
    }
    
    public Calendar getTimePointCalendar() {
        return this.mTimePointCalendar;
    }
}
