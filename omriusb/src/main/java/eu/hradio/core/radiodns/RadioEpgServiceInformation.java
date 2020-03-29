package eu.hradio.core.radiodns;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import eu.hradio.core.radiodns.radioepg.serviceinformation.*;
import java.util.*;

public class RadioEpgServiceInformation implements Serializable
{
    private static final long serialVersionUID = -9110375812503863251L;
    private final TimePoint mCreationTime;
    private final String mOriginator;
    private final String mLanguage;
    private final int mVersion;
    private final String mTermsUrl;
    private ServiceProvider mServiceProvider;
    private List<Service> mServices;
    
    RadioEpgServiceInformation(final String creationTime, final String originator, final String language, final int version, final String termsUrl) {
        this.mServices = new ArrayList<Service>();
        this.mCreationTime = new TimePoint(creationTime);
        this.mOriginator = originator;
        this.mLanguage = language;
        this.mVersion = version;
        this.mTermsUrl = termsUrl;
    }
    
    RadioEpgServiceInformation(final String creationTime, final String originator, final String language, final int version) {
        this(creationTime, originator, language, version, "");
    }
    
    RadioEpgServiceInformation(final String creationTime, final String originator, final String language) {
        this(creationTime, originator, language, 1, "");
    }
    
    void setServiceProvider(final ServiceProvider provider) {
        this.mServiceProvider = provider;
    }
    
    void addService(final Service service) {
        this.mServices.add(service);
    }
    
    void addServices(final List<Service> services) {
        this.mServices.addAll(services);
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
    
    public int getVersion() {
        return this.mVersion;
    }
    
    public String getTermsUrl() {
        return this.mTermsUrl;
    }
    
    public ServiceProvider getServiceProvider() {
        return this.mServiceProvider;
    }
    
    public List<Service> getServices() {
        return this.mServices;
    }
}
