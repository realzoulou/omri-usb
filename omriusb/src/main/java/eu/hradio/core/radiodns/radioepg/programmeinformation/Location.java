package eu.hradio.core.radiodns.radioepg.programmeinformation;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import java.util.*;

public class Location implements Serializable
{
    private static final long serialVersionUID = 6722895495494377335L;
    private List<Time> mTimes;
    private List<RelativeTime> mRelativeTimes;
    private List<Bearer> mBearers;
    
    public Location() {
        this.mTimes = new ArrayList<Time>();
        this.mRelativeTimes = new ArrayList<RelativeTime>();
        this.mBearers = new ArrayList<Bearer>();
    }
    
    public void addTime(final Time time) {
        this.mTimes.add(time);
    }
    
    public void addRelativeTime(final RelativeTime relTime) {
        this.mRelativeTimes.add(relTime);
    }
    
    public void addBearer(final Bearer bearer) {
        this.mBearers.add(bearer);
    }
    
    public List<Time> getTimes() {
        return this.mTimes;
    }
    
    public List<RelativeTime> getRelativeTimes() {
        return this.mRelativeTimes;
    }
    
    public List<Bearer> getBearers() {
        return this.mBearers;
    }
}
