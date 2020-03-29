package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import eu.hradio.core.radiodns.radioepg.scope.*;
import androidx.annotation.NonNull;
import java.util.*;

public class Schedule implements Serializable
{
    private static final long serialVersionUID = -8949115745750085336L;
    private final TimePoint mCreationTime;
    private final String mOriginator;
    private final int mVersion;
    private final String mLanguage;
    private Scope mScope;
    private List<Programme> mProgrammes;
    
    public Schedule(@NonNull final String creationTime, @NonNull final String originator, final int version, @NonNull final String language) {
        this.mProgrammes = new ArrayList<Programme>();
        this.mCreationTime = new TimePoint(creationTime);
        this.mOriginator = originator;
        this.mVersion = version;
        this.mLanguage = language;
    }
    
    public Schedule(@NonNull final String creationTime, @NonNull final String originator, final int version) {
        this(creationTime, originator, version, "en");
    }
    
    public Schedule(@NonNull final String creationTime, @NonNull final String originator) {
        this(creationTime, originator, 1, "en");
    }
    
    public Schedule(@NonNull final String creationTime) {
        this(creationTime, "", 1, "en");
    }
    
    public Schedule() {
        this("", "", 1, "en");
    }
    
    public void setScope(final Scope scope) {
        this.mScope = scope;
    }
    
    public void addProgramme(final Programme programme) {
        this.mProgrammes.add(programme);
    }
    
    public TimePoint getCreationTime() {
        return this.mCreationTime;
    }
    
    public String getOriginator() {
        return this.mOriginator;
    }
    
    public int getVersion() {
        return this.mVersion;
    }
    
    public String getLanguage() {
        return this.mLanguage;
    }
    
    public Scope getScope() {
        return this.mScope;
    }
    
    public List<Programme> getProgrammes() {
        return this.mProgrammes;
    }
    
    public Programme getCurrentRunningProgramme() {
        Programme retProg = null;
        for (final Programme prog : this.mProgrammes) {
            for (final Location loc : prog.getLocations()) {
                for (final Time progTime : loc.getTimes()) {
                    final long progStartTimeMillis = progTime.getStartTime().getTimePointCalendar().getTimeInMillis();
                    final long progStopTimeMillis = progStartTimeMillis + progTime.getDuration().getDurationSeconds() * 1000;
                    final long curTime = System.currentTimeMillis();
                    if (curTime >= progStartTimeMillis && curTime < progStopTimeMillis) {
                        retProg = prog;
                        break;
                    }
                }
            }
        }
        return retProg;
    }
}
