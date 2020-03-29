package eu.hradio.core.radiodns;

public enum RadioDnsServiceType
{
    RADIO_VIS("radiovis"), 
    RADIO_EPG("radioepg"), 
    RADIO_TAG("radiotag"), 
    RADIO_WEB("radioweb");
    
    private final String mAppName;
    
    private RadioDnsServiceType(final String appName) {
        this.mAppName = appName;
    }
    
    public String getAppName() {
        return this.mAppName;
    }
}
