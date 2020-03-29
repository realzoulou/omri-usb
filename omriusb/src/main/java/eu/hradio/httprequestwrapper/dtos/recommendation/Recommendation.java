package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;

public class Recommendation implements Serializable
{
    private static final long serialVersionUID = -3390546577904585820L;
    private double score;
    private String serviceHash;
    
    public double getScore() {
        return this.score;
    }
    
    public void setScore(final double score) {
        this.score = score;
    }
    
    public String getServiceHash() {
        return this.serviceHash;
    }
    
    public void setServiceHash(final String serviceHash) {
        this.serviceHash = serviceHash;
    }
}
