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

public class ProgrammeEvent implements Serializable
{
    private static final long serialVersionUID = 5123087057963703256L;
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
    
    public ProgrammeEvent(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation, @NonNull final BroadcastType bcType, @NonNull final String language) {
        this.mNames = new ArrayList<Name>();
        this.mLocations = new ArrayList<Location>();
        this.mOndemands = new ArrayList<OnDemand>();
        this.mMediaDescriptions = new ArrayList<MediaDescription>();
        this.mGenres = new ArrayList<Genre>();
        this.mKeywords = new ArrayList<Keywords>();
        this.mLinks = new ArrayList<Link>();
        this.mShortId = shortId;
        this.mId = id;
        this.mVersion = version;
        this.mIsRecommendation = recommendation;
        this.mBroadcastType = bcType;
        this.mLanguage = language;
    }
    
    public ProgrammeEvent(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation, @NonNull final BroadcastType bcType) {
        this(shortId, id, version, recommendation, bcType, "en");
    }
    
    public ProgrammeEvent(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version, final boolean recommendation) {
        this(shortId, id, version, recommendation, BroadcastType.BROADCAST_TYPE_ON_AIR);
    }
    
    public ProgrammeEvent(@NonNull final ShortCrid shortId, @NonNull final Crid id, final int version) {
        this(shortId, id, version, false);
    }
    
    public ProgrammeEvent(@NonNull final ShortCrid shortId, @NonNull final Crid id) {
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
}
