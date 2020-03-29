package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;

public class RankedStandaloneService implements Serializable
{
    private static final long serialVersionUID = -3922168963475026927L;
    private double score;
    private String source;
    private StandaloneService content;
    
    public double getScore() {
        return this.score;
    }
    
    public void setScore(final double score) {
        this.score = score;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public StandaloneService getContent() {
        return this.content;
    }
    
    public void setContent(final StandaloneService service) {
        this.content = service;
    }
}
