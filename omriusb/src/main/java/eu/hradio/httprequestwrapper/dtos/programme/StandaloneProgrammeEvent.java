package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;
import java.util.*;
import eu.hradio.httprequestwrapper.util.*;

public class StandaloneProgrammeEvent implements Serializable
{
    private static final long serialVersionUID = 8633226974209139763L;
    private long id;
    private long parentId;
    private Date startTime;
    private Date stopTime;
    private String content;
    private String genres;
    
    public long getParentId() {
        return this.parentId;
    }
    
    public void setParentId(final long parentId) {
        this.parentId = parentId;
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
    
    public String getContent() {
        return this.content;
    }
    
    public void setContent(final String content) {
        this.content = content;
    }
    
    public String getGenres() {
        return this.genres;
    }
    
    public void setGenres(final String genres) {
        this.genres = genres;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
}
