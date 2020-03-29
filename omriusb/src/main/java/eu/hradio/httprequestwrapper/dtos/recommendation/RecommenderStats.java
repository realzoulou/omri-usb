package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;

public class RecommenderStats implements Serializable
{
    private static final long serialVersionUID = -818667763064470381L;
    private Recommender[] recommenderStats;
    
    public Recommender[] getRecommenderStats() {
        return this.recommenderStats;
    }
    
    public void setRecommenderStats(final Recommender[] recommenderStats) {
        this.recommenderStats = recommenderStats;
    }
}
