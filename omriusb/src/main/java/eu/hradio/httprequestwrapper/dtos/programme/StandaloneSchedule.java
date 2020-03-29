package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;
import java.util.*;
import eu.hradio.httprequestwrapper.util.*;

public class StandaloneSchedule implements Serializable
{
    private static final long serialVersionUID = 6504697912115317549L;
    private long id;
    private long parentId;
    private Date startTime;
    private Date stopTime;
    private long[] programmes;
    
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
    
    public long[] getProgrammes() {
        return this.programmes;
    }
    
    public void setProgrammes(final long[] programmes) {
        this.programmes = programmes;
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
}
