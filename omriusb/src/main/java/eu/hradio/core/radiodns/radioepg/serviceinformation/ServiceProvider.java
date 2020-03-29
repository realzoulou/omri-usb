package eu.hradio.core.radiodns.radioepg.serviceinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.mediadescription.*;
import eu.hradio.core.radiodns.radioepg.keywords.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import eu.hradio.core.radiodns.radioepg.geolocation.*;
import java.util.*;

public class ServiceProvider implements Serializable
{
    private static final long serialVersionUID = 5657446199968357120L;
    private List<Name> mNames;
    private List<MediaDescription> mMediaDescriptions;
    private Keywords mKeywords;
    private List<Link> mLinks;
    private GeoLocation mGeoLocation;
    
    public ServiceProvider() {
        this.mNames = new ArrayList<Name>();
        this.mMediaDescriptions = new ArrayList<MediaDescription>();
        this.mLinks = new ArrayList<Link>();
    }
    
    public void addName(final Name name) {
        this.mNames.add(name);
    }
    
    public void addMediaDescription(final MediaDescription description) {
        this.mMediaDescriptions.add(description);
    }
    
    public void setKeywords(final String kw) {
        this.mKeywords = new Keywords(kw);
    }
    
    public void addLink(final Link link) {
        this.mLinks.add(link);
    }
    
    public void setGeolocation(final GeoLocation geo) {
        this.mGeoLocation = geo;
    }
    
    public List<Name> getNames() {
        return this.mNames;
    }
    
    public List<MediaDescription> getMediaDescriptions() {
        return this.mMediaDescriptions;
    }
    
    public Keywords getKeywords() {
        return this.mKeywords;
    }
    
    public List<Link> getLinks() {
        return this.mLinks;
    }
    
    public GeoLocation getGeoLocation() {
        return this.mGeoLocation;
    }
}
