package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class Context implements Serializable
{
    private static final long serialVersionUID = 6392319215196828824L;
    private Demographics demographics;
    private String time;
    
    public Demographics getDemographics() {
        return this.demographics;
    }
    
    public void setDemographics(final Demographics demographics) {
        this.demographics = demographics;
    }
    
    public String getTime() {
        return this.time;
    }
    
    public void setTime(final String time) {
        this.time = time;
    }
}
