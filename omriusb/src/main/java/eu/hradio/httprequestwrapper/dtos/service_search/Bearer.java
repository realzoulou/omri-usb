package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;

public class Bearer implements Serializable
{
    private static final long serialVersionUID = -3681390299223238057L;
    private String address;
    private int bitrate;
    private BearerType type;
    private String mimeType;
    
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(final String address) {
        this.address = address;
    }
    
    public int getBitrate() {
        return this.bitrate;
    }
    
    public void setBitrate(final int bitrate) {
        this.bitrate = bitrate;
    }
    
    public BearerType getType() {
        return this.type;
    }
    
    public void setType(final BearerType type) {
        this.type = type;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }
    
    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }
}
