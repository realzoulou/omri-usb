package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.crid.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.mediadescription.*;
import eu.hradio.core.radiodns.radioepg.genre.*;
import eu.hradio.core.radiodns.radioepg.keywords.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import androidx.annotation.NonNull;
import java.util.*;

public class Programme implements Serializable
{
    private static final long serialVersionUID = 1044907069207230679L;
    private final ShortCrid mShortId;
    private final Crid mId;
    private final boolean mIsRecommendation;
    private final BroadcastType mBroadcastType;
    private final String mLanguage;
    private final int mVersion;
    private List<Name> mNames;
    private List<Location> mLocations;
    private List<OnDemand> mOndemands;
    private List<MediaDescription> mMediaDescriptions;
    private List<Genre> mGenres;
    private List<Keywords> mKeywords;
    private List<Link> mLinks;
    private List<ProgrammeEvent> mProgrammeEvents;
    
    public Programme(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation, @NonNull final BroadcastType bcType, @NonNull final String language) {
        this.mNames = new ArrayList<Name>();
        this.mLocations = new ArrayList<Location>();
        this.mOndemands = new ArrayList<OnDemand>();
        this.mMediaDescriptions = new ArrayList<MediaDescription>();
        this.mGenres = new ArrayList<Genre>();
        this.mKeywords = new ArrayList<Keywords>();
        this.mLinks = new ArrayList<Link>();
        this.mProgrammeEvents = new ArrayList<ProgrammeEvent>();
        this.mShortId = shortId;
        this.mId = id;
        this.mVersion = version;
        this.mIsRecommendation = recommendation;
        this.mBroadcastType = bcType;
        this.mLanguage = language;
    }
    
    public Programme(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation, @NonNull final BroadcastType bcType) {
        this(shortId, id, version, recommendation, bcType, "en");
    }
    
    public Programme(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation) {
        this(shortId, id, version, recommendation, BroadcastType.BROADCAST_TYPE_ON_AIR);
    }
    
    public Programme(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version) {
        this(shortId, id, version, false);
    }
    
    public Programme(@NonNull final ShortCrid shortId, @NonNull final Crid id) {
        this(shortId, id, 1);
    }
    
    public void addName(final Name name) {
        this.mNames.add(name);
    }
    
    public void addLocation(final Location location) {
        this.mLocations.add(location);
    }
    
    public void addOnDemand(final OnDemand ondemand) {
        this.mOndemands.add(ondemand);
    }
    
    public void addMediaDescription(final MediaDescription md) {
        this.mMediaDescriptions.add(md);
    }
    
    public void addGenre(final Genre genre) {
        this.mGenres.add(genre);
    }
    
    public void addKeywords(final Keywords kw) {
        this.mKeywords.add(kw);
    }
    
    public void addLink(final Link link) {
        this.mLinks.add(link);
    }
    
    public void addProgrammeEvent(final ProgrammeEvent event) {
        this.mProgrammeEvents.add(event);
    }
    
    public ShortCrid getShortId() {
        return this.mShortId;
    }
    
    public Crid getId() {
        return this.mId;
    }
    
    public boolean isRecommendation() {
        return this.mIsRecommendation;
    }
    
    public BroadcastType getBroadcastType() {
        return this.mBroadcastType;
    }
    
    public String getLanguage() {
        return this.mLanguage;
    }
    
    public int getVersion() {
        return this.mVersion;
    }
    
    public List<Name> getNames() {
        return this.mNames;
    }
    
    public List<Location> getLocations() {
        return this.mLocations;
    }
    
    public List<OnDemand> getOndemands() {
        return this.mOndemands;
    }
    
    public List<MediaDescription> getMediaDescriptions() {
        return this.mMediaDescriptions;
    }
    
    public List<Genre> getGenres() {
        return this.mGenres;
    }
    
    public List<Keywords> getKeywords() {
        return this.mKeywords;
    }
    
    public List<Link> getLinks() {
        return this.mLinks;
    }
    
    public List<ProgrammeEvent> getProgrammeEvents() {
        return this.mProgrammeEvents;
    }
    
    public int getRemainingSeconds() {
        int remainingSeconds = -1;
        for (final Location loc : this.mLocations) {
            for (final Time progTime : loc.getTimes()) {
                final long progStartTimeMillis = progTime.getStartTime().getTimePointCalendar().getTimeInMillis();
                final long progStopTimeMillis = progStartTimeMillis + progTime.getDuration().getDurationSeconds() * 1000;
                final long curTime = System.currentTimeMillis();
                if (curTime < progStartTimeMillis) {
                    remainingSeconds = (int)((progStopTimeMillis - progStartTimeMillis) / 1000L);
                }
                else if (curTime >= progStopTimeMillis) {
                    remainingSeconds = 0;
                }
                else {
                    remainingSeconds = (int)((progStopTimeMillis - curTime) / 1000L);
                }
            }
        }
        return remainingSeconds;
    }
}
