package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class ServiceUseResult implements Serializable
{
    private static final long serialVersionUID = -4899274927107366329L;
    private Context context;
    private String[] services;
    private long id;
    
    public Context getContext() {
        return this.context;
    }
    
    public void setContext(final Context context) {
        this.context = context;
    }
    
    public String[] getServices() {
        return this.services;
    }
    
    public void setServices(final String[] services) {
        this.services = services;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
}
