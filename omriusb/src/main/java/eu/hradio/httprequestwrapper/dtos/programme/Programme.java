package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;
import java.util.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;

public class Programme implements Serializable
{
    private static final long serialVersionUID = -5218046155216246305L;
    private long id;
    private long parentId;
    private Date startTime;
    private Date stopTime;
    private long[] programmeEvents;
    private MediaDescription[] mediaDescriptions;
    private String name;
    private String longName;
    private String description;
    private String genres;
    private String features;
    private String hash;
    
    public Date getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getStopTime() {
        return this.stopTime;
    }
    
    public void setStopTime(final Date stopTime) {
        this.stopTime = stopTime;
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
    
    public long[] getProgrammeEvents() {
        return this.programmeEvents;
    }
    
    public void setProgrammeEvents(final long[] programmeEvents) {
        this.programmeEvents = programmeEvents;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public long getParentId() {
        return this.parentId;
    }
    
    public void setParentId(final long parentId) {
        this.parentId = parentId;
    }
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(final String hash) {
        this.hash = hash;
    }
}
