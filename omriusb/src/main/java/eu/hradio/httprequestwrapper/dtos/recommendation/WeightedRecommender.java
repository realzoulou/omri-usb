package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;

public class WeightedRecommender implements Serializable
{
    private static final long serialVersionUID = -5989466427780752250L;
    private Recommender recommender;
    private double weigth;
    
    public WeightedRecommender(final Recommender recommender, final double weigth) {
        this.weigth = weigth;
        this.recommender = recommender;
    }
    
    public Recommender getRecommender() {
        return this.recommender;
    }
    
    public void setRecommender(final Recommender recommender) {
        this.recommender = recommender;
    }
    
    public double getWeigth() {
        return this.weigth;
    }
    
    public void setWeigth(final double weigth) {
        this.weigth = weigth;
    }
}
