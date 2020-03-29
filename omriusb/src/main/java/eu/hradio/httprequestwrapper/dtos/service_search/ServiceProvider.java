package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;

public class ServiceProvider implements Serializable
{
    private static final long serialVersionUID = 3700691959329907573L;
    private long id;
    private String name;
    private String description;
    private long[] services;
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public long[] getServices() {
        return this.services;
    }
    
    public void setServices(final long[] services) {
        this.services = services;
    }
}
