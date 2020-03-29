package eu.hradio.core.radiodns.radioepg.scope;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;
import java.util.*;

public class Scope implements Serializable
{
    private static final long serialVersionUID = -3684242241886246262L;
    private final TimePoint mStartTime;
    private final TimePoint mStopTime;
    private List<ServiceScope> mServiceScopes;
    
    public Scope(@NonNull final String startTime, @NonNull final String stopTime) {
        this.mServiceScopes = new ArrayList<ServiceScope>();
        this.mStartTime = new TimePoint(startTime);
        this.mStopTime = new TimePoint(stopTime);
    }
    
    public void addServiceScope(final ServiceScope scope) {
        this.mServiceScopes.add(scope);
    }
    
    public TimePoint getStartTime() {
        return this.mStartTime;
    }
    
    public TimePoint getStopTime() {
        return this.mStopTime;
    }
    
    public List<ServiceScope> getServiceScopes() {
        return this.mServiceScopes;
    }
}
