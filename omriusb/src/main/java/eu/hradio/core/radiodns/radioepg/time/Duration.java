package eu.hradio.core.radiodns.radioepg.time;

import java.io.*;
import androidx.annotation.NonNull;

public class Duration implements Serializable
{
    private static final long serialVersionUID = 5563294957799955046L;
    private final String mDurationString;
    private final int mDurationSeconds;
    
    public Duration(@NonNull final String duration) {
        this.mDurationString = duration.trim();
        String durString = "";
        if (this.mDurationString.length() > 0) {
            durString = this.mDurationString.substring(2);
        }
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        if (durString.length() > 0 && this.mDurationString.contains("H")) {
            final String hoursString = durString.split("H")[0];
            hours = Integer.parseInt(hoursString) * 60 * 60;
            durString = this.mDurationString.substring(this.mDurationString.indexOf("H") + 1);
        }
        if (durString.length() > 0 && this.mDurationString.contains("M")) {
            final String minutesString = durString.split("M")[0];
            minutes = Integer.parseInt(minutesString) * 60;
            durString = this.mDurationString.substring(this.mDurationString.indexOf("M") + 1);
        }
        if (durString.length() > 0 && this.mDurationString.contains("S")) {
            final String secondsString = durString.split("S")[0];
            seconds = Integer.parseInt(secondsString);
        }
        this.mDurationSeconds = hours + minutes + seconds;
    }
    
    public int getDurationSeconds() {
        return this.mDurationSeconds;
    }
}
