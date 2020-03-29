package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;
import eu.hradio.httprequestwrapper.dtos.service_use.*;

public class StandaloneService implements Serializable
{
    private static final long serialVersionUID = -7379243126214629943L;
    private long id;
    private long parentId;
    private String name;
    private String description;
    private long[] schedules;
    private String genres;
    private String features;
    private Bearer[] bearers;
    private MediaDescription[] mediaDescriptions;
    private String providerName;
    private String keywords;
    private Location location;
    private String hash;
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public long getParentId() {
        return this.parentId;
    }
    
    public void setParentId(final long parentId) {
        this.parentId = parentId;
    }
    
    public long[] getSchedules() {
        return this.schedules;
    }
    
    public void setSchedules(final long[] schedules) {
        this.schedules = schedules;
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
    
    public Bearer[] getBearers() {
        return this.bearers;
    }
    
    public void setBearers(final Bearer[] bearers) {
        this.bearers = bearers;
    }
    
    public MediaDescription[] getMediaDescriptions() {
        return this.mediaDescriptions;
    }
    
    public void setMediaDescriptions(final MediaDescription[] mediaDescriptions) {
        this.mediaDescriptions = mediaDescriptions;
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(final Location location) {
        this.location = location;
    }
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(final String hash) {
        this.hash = hash;
    }
    
    public String getProviderName() {
        return this.providerName;
    }
    
    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }
    
    public String getKeywords() {
        return this.keywords;
    }
    
    public void setKeywords(final String keywords) {
        this.keywords = keywords;
    }
}
