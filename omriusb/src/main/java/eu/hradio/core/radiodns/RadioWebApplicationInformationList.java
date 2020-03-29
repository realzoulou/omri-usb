package eu.hradio.core.radiodns;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import java.util.*;

public class RadioWebApplicationInformationList implements Serializable
{
    private static final long serialVersionUID = 8587869123685218943L;
    private static final String TAG = "RWebAIL";
    private final TimePoint mCreationTime;
    private final String mOriginator;
    private final String mLanguage;
    private List<RadioWebApplication> mApps;
    
    RadioWebApplicationInformationList(final String creationTime, final String originator, final String language) {
        this.mApps = new ArrayList<RadioWebApplication>();
        this.mCreationTime = new TimePoint(creationTime);
        this.mOriginator = originator;
        this.mLanguage = language;
    }
    
    void addApplication(final RadioWebApplication app) {
        this.mApps.add(app);
    }
    
    public TimePoint getCreationTime() {
        return this.mCreationTime;
    }
    
    public String getOriginator() {
        return this.mOriginator;
    }
    
    public String getLanguage() {
        return this.mLanguage;
    }
    
    public List<RadioWebApplication> getRadioWebApps() {
        return this.mApps;
    }
}
