package eu.hradio.core.radiodns.radioepg.serviceinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.mediadescription.*;
import eu.hradio.core.radiodns.radioepg.genre.*;
import eu.hradio.core.radiodns.radioepg.keywords.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import eu.hradio.core.radiodns.radioepg.radiodns.*;
import eu.hradio.core.radiodns.radioepg.geolocation.*;
import java.util.*;
import androidx.annotation.NonNull;

public class Service implements Serializable
{
    private static final long serialVersionUID = -4223533420411915065L;
    private final int mVersion;
    private List<Name> mNames;
    private List<MediaDescription> mMediaDescriptions;
    private List<Genre> mGenres;
    private Keywords mKeywords;
    private List<Link> mLinks;
    private List<Bearer> mBearers;
    private RadioDns mRadioDns;
    private GeoLocation mGeoLocation;
    
    public Service(final int version) {
        this.mNames = new ArrayList<Name>();
        this.mMediaDescriptions = new ArrayList<MediaDescription>();
        this.mGenres = new ArrayList<Genre>();
        this.mKeywords = new Keywords("");
        this.mLinks = new ArrayList<Link>();
        this.mBearers = new ArrayList<Bearer>();
        this.mVersion = version;
    }
    
    public Service() {
        this(1);
    }
    
    public void addName(@NonNull final Name name) {
        this.mNames.add(name);
    }
    
    public void addMediaDescription(@NonNull final MediaDescription md) {
        this.mMediaDescriptions.add(md);
    }
    
    public void addGenre(@NonNull final Genre genre) {
        this.mGenres.add(genre);
    }
    
    public void setKeywords(final String kw) {
        this.mKeywords = new Keywords(kw);
    }
    
    public void addLink(final Link link) {
        this.mLinks.add(link);
    }
    
    public void addBearer(final Bearer bearer) {
        this.mBearers.add(bearer);
    }
    
    public void setRadioDns(final RadioDns rdns) {
        this.mRadioDns = rdns;
    }
    
    public void setGeoLocation(final GeoLocation geo) {
        this.mGeoLocation = geo;
    }
    
    public int getVersion() {
        return this.mVersion;
    }
    
    public List<Name> getNames() {
        return this.mNames;
    }
    
    public List<MediaDescription> getMediaDescriptions() {
        return this.mMediaDescriptions;
    }
    
    public List<Genre> getGenres() {
        return this.mGenres;
    }
    
    public Keywords getKeywords() {
        return this.mKeywords;
    }
    
    public List<Link> getLinks() {
        return this.mLinks;
    }
    
    public List<Bearer> getBearers() {
        return this.mBearers;
    }
    
    public RadioDns getRadioDns() {
        return this.mRadioDns;
    }
    
    public GeoLocation getGeoLocation() {
        return this.mGeoLocation;
    }
}
