package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;
import eu.hradio.httprequestwrapper.dtos.service_use.*;
import java.util.*;

public class RecommendationRequest implements Serializable
{
    private static final long serialVersionUID = 4837288529846445809L;
    private HashMap<String, Double> recommenders;
    private String serviceHash;
    private Context context;
    
    public RecommendationRequest(final HashMap<String, Double> recommenders, final String serviceHash, final Context context) {
        this.recommenders = recommenders;
        this.serviceHash = serviceHash;
        this.context = context;
    }
    
    public RecommendationRequest() {
    }
    
    public Map<String, Double> getRecommenders() {
        return this.recommenders;
    }
    
    public void setRecommenders(final HashMap<String, Double> recommenders) {
        this.recommenders = recommenders;
    }
    
    public String getServiceHash() {
        return this.serviceHash;
    }
    
    public void setServiceHash(final String serviceHash) {
        this.serviceHash = serviceHash;
    }
    
    public Context getContext() {
        return this.context;
    }
    
    public void setContext(final Context context) {
        this.context = context;
    }
}
