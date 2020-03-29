package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;

public class Genre implements Serializable
{
    private static final long serialVersionUID = 8431749147139858553L;
    private String name;
    private String description;
    private double ratio;
    
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
    
    public double getRatio() {
        return this.ratio;
    }
    
    public void setRatio(final double ratio) {
        this.ratio = ratio;
    }
    
    @Override
    public String toString() {
        return this.description;
    }
}
