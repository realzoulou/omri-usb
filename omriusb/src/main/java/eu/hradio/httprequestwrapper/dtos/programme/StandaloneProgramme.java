package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;
import java.util.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;
import eu.hradio.httprequestwrapper.util.*;

public class StandaloneProgramme implements Serializable
{
    private static final long serialVersionUID = 3029994036274271495L;
    private long id;
    private long serviceId;
    private long providerId;
    private Date startTime;
    private Date stopTime;
    private ProgrammeEvent[] programmeEvents;
    private String longName;
    private String description;
    private String genres;
    private String features;
    private MediaDescription[] mediaDescriptions;
    private String name;
    private String hash;
    private String serviceHash;
    private WebContent[] webContents;
    
    public WebContent[] getWebContents() {
        return this.webContents;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public long getServiceId() {
        return this.serviceId;
    }
    
    public void setServiceId(final long serviceId) {
        this.serviceId = serviceId;
    }
    
    public long getProviderId() {
        return this.providerId;
    }
    
    public void setProviderId(final long providerId) {
        this.providerId = providerId;
    }
    
    public Date getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(final String startTime) {
        this.startTime = TimeUtils.dateFromString(startTime);
    }
    
    public Date getStopTime() {
        return this.stopTime;
    }
    
    public void setStopTime(final String stopTime) {
        this.stopTime = TimeUtils.dateFromString(stopTime);
    }
    
    public ProgrammeEvent[] getProgrammeEvents() {
        return this.programmeEvents;
    }
    
    public void setProgrammeEvents(final ProgrammeEvent[] programmeEvents) {
        this.programmeEvents = programmeEvents;
    }
    
    public MediaDescription[] getMediaDescriptions() {
        return this.mediaDescriptions;
    }
    
    public void setMediaDescriptions(final MediaDescription[] mediaDescriptions) {
        this.mediaDescriptions = mediaDescriptions;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getLongName() {
        return this.longName;
    }
    
    public void setLongName(final String longName) {
        this.longName = longName;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getGenres() {
        return this.genres;
    }
    
    public void setGenres(final String genres) {
        this.genres = genres;
    }
    
    public String getFeatures() {
        return this.features;
    }
    
    public void setFeatures(final String features) {
        this.features = features;
    }
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(final String hash) {
        this.hash = hash;
    }
    
    public String getServiceHash() {
        return this.serviceHash;
    }
    
    public void setServiceHash(final String serviceHash) {
        this.serviceHash = serviceHash;
    }
}
