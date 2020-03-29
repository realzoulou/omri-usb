package eu.hradio.httprequestwrapper.service;

import java.util.*;
import eu.hradio.httprequestwrapper.dtos.service_use.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;
import eu.hradio.httprequestwrapper.dtos.recommendation.*;

public interface RecommendationClient extends HRadioHttpClient
{
    void asyncRecommendationRequestName(final List<WeightedRecommender> p0, final String p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4, final boolean p5, final double p6);
    
    void asyncRecommendationRequestName(final List<WeightedRecommender> p0, final String p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4, final boolean p5);
    
    void asyncRecommendationRequestName(final List<WeightedRecommender> p0, final String p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4);
    
    void asyncRecommendationRequest(final List<WeightedRecommender> p0, final StandaloneService p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4, final boolean p5, final double p6);
    
    void asyncRecommendationRequest(final List<WeightedRecommender> p0, final StandaloneService p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4, final boolean p5);
    
    void asyncRecommendationRequest(final List<WeightedRecommender> p0, final StandaloneService p1, final Context p2, final OnSearchResultListener<ServiceList> p3, final OnErrorListener p4);
    
    void asyncGetAvailableRecommenders(final OnSearchResultListener<RecommenderStats> p0, final OnErrorListener p1);
    
    void asyncRecommendationRequestByName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3, final double p4);
    
    void asyncRecommendationRequestByName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2);
    
    void asyncRecommendationRequestByName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3);
    
    void asyncRecommendationRequest(final StandaloneService p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3, final double p4);
    
    void asyncRecommendationRequest(final StandaloneService p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2);
    
    void asyncRecommendationRequest(final StandaloneService p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3);
}
