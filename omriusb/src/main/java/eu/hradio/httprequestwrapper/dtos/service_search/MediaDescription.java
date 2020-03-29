package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;

public class MediaDescription implements Serializable
{
    private String longDescription;
    private String shortDescription;
    private String multimediaType;
    private String url;
    private String mimeValue;
    private int width;
    private int height;
    private String type;
    
    public String getLongDescription() {
        return this.longDescription;
    }
    
    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }
    
    public String getShortDescription() {
        return this.shortDescription;
    }
    
    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public String getMultimediaType() {
        return this.multimediaType;
    }
    
    public void setMultimediaType(final String multimediaType) {
        this.multimediaType = multimediaType;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    public String getMimeValue() {
        return this.mimeValue;
    }
    
    public void setMimeValue(final String mimeValue) {
        this.mimeValue = mimeValue;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setWidth(final int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public void setHeight(final int height) {
        this.height = height;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
}
