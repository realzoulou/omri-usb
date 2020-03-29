package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;

public class WebContent implements Serializable
{
    private static final long serialVersionUID = 6934190593789994259L;
    private String link;
    private String mimeType;
    private String description;
    
    public String getUrl() {
        return this.link;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }
    
    public String getDescription() {
        return this.description;
    }
}
