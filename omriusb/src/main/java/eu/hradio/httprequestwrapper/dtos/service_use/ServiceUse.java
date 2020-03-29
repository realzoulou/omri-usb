package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class ServiceUse implements Serializable
{
    private static final long serialVersionUID = 2702317337918159365L;
    private Context context;
    private String[] services;
    
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
}
