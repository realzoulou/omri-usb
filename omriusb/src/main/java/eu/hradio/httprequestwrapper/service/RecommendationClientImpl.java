package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.service_use.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.query.*;
import eu.hradio.httprequestwrapper.parser.*;
import eu.hradio.httprequestwrapper.exception.*;
import eu.hradio.httprequestwrapper.dtos.recommendation.*;
import java.util.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;
import eu.hradio.httprequestwrapper.util.*;

public class RecommendationClientImpl extends HRadioHttpClientImpl implements RecommendationClient
{
    private static final String TAG;
    
    @Override
    public void asyncRecommendationRequestName(final List<WeightedRecommender> recommenders, final String name, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter, final double threshold) {
        final HRadioQuery searchForServiceHashQuery = new HRadioQueryImpl();
        searchForServiceHashQuery.addEndPoint("services");
        searchForServiceHashQuery.append("q", "name:\"" + name + "\"");
        this.asyncRequest(searchForServiceHashQuery, res -> {
            if (res.getContent().length > 1) {
                errorListener.onError(new NameNotUniqueException(searchForServiceHashQuery.toUrlString()));
            }
            if (res.getContent().length >= 1) {
                this.asyncRecommendationRequest(recommenders, res.getContent()[0].getContent(), context, listener, errorListener, filter, threshold);
            }
            else {
                errorListener.onError(new NoResultFoundException(searchForServiceHashQuery.toUrlString()));
            }
        }, errorListener, ServiceList.class);
    }
    
    @Override
    public void asyncRecommendationRequest(final List<WeightedRecommender> recommenders, final StandaloneService service, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter, final double threshold) {
        final HashMap<String, Double> map = new HashMap<String, Double>();
        for (final WeightedRecommender recommender : recommenders) {
            map.put(recommender.getRecommender().getRecommenderName(), recommender.getWeigth());
        }
        final RecommendationRequest request = new RecommendationRequest(map, service.getHash(), context);
        final HRadioQuery recommenderQuery = new HRadioQueryImpl();
        recommenderQuery.setPort(8100);
        recommenderQuery.addEndPoint("recommendations");
        recommenderQuery.setRequestMethod("POST");
        try {
            recommenderQuery.setBody(new JsonParser().toJSON(request).toString());
            this.asyncRequest(recommenderQuery, res -> this.resolveServicesHashes(service, res, listener, errorListener, filter, threshold), errorListener, RecommendationList.class);
        }
        catch (JsonEncoderTypeMismatch jsonEncoderTypeMismatch) {
            errorListener.onError(jsonEncoderTypeMismatch);
        }
    }
    
    @Override
    public void asyncGetAvailableRecommenders(final OnSearchResultListener<RecommenderStats> listener, final OnErrorListener errorListener) {
        final HRadioQuery recommenderQuery = new HRadioQueryImpl();
        recommenderQuery.setPort(8100);
        recommenderQuery.addEndPoint("recommender_stats");
        this.asyncRequest(recommenderQuery, listener, errorListener, RecommenderStats.class);
    }
    
    @Override
    public void asyncRecommendationRequest(final StandaloneService service, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter, final double threshold) {
        final HRadioQuery recommenderQuery = new HRadioQueryImpl();
        recommenderQuery.setPort(8100);
        recommenderQuery.addEndPoint("recommendations");
        recommenderQuery.append("serviceHash", service.getHash());
        this.asyncRequest(recommenderQuery, res -> this.resolveServicesHashes(service, res, listener, errorListener, filter, threshold), errorListener, RecommendationList.class);
    }
    
    @Override
    public void asyncRecommendationRequestByName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter, final double threshold) {
        final HRadioQuery searchForServiceHashQuery = new HRadioQueryImpl();
        searchForServiceHashQuery.addEndPoint("services");
        searchForServiceHashQuery.append("q", "name:\"" + name + "\"");
        this.asyncRequest(searchForServiceHashQuery, res -> {
            if (res.getContent().length > 1) {
                errorListener.onError(new NameNotUniqueException(searchForServiceHashQuery.toUrlString()));
            }
            if (res.getContent().length >= 1) {
                this.asyncRecommendationRequest(res.getContent()[0].getContent(), listener, errorListener, filter, threshold);
            }
            else {
                errorListener.onError(new NoResultFoundException(searchForServiceHashQuery.toUrlString()));
            }
        }, errorListener, ServiceList.class);
    }
    
    private void resolveServicesHashes(final StandaloneService service, final RecommendationList list, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filterSimilar, final double threshold) {
        final StringBuilder recommendedHashBuilder = new StringBuilder();
        final Iterator<Recommendation> iterator = Arrays.asList(list.getContent()).iterator();
        while (iterator.hasNext()) {
            recommendedHashBuilder.append(iterator.next().getServiceHash());
            if (iterator.hasNext()) {
                recommendedHashBuilder.append(",");
            }
        }
        final HRadioQuery resolverQuery = new HRadioQueryImpl();
        resolverQuery.addEndPoint("services");
        resolverQuery.append("q", "(" + recommendedHashBuilder.toString() + ")");
        OnSearchResultListener<ServiceList> resultListener;
        if (filterSimilar) {
            resultListener = (resolvedList -> listener.onResult(this.preProcessResult(resolvedList, service, threshold)));
        }
        else {
            resultListener = listener;
        }
        this.asyncRequest(resolverQuery, resultListener, errorListener, ServiceList.class);
    }
    
    private ServiceList preProcessResult(final ServiceList recommendationList, final StandaloneService service, final double threshold) {
        final RankedStandaloneService[] rawRecommendations = recommendationList.getContent();
        final List<RankedStandaloneService> filteredRecommendations = new ArrayList<RankedStandaloneService>();
        for (final RankedStandaloneService rawService : rawRecommendations) {
            if (!this.filterService(rawService.getContent(), service, threshold)) {
                filteredRecommendations.add(rawService);
            }
        }
        recommendationList.setContent(filteredRecommendations.toArray(new RankedStandaloneService[0]));
        return recommendationList;
    }
    
    private boolean filterService(final StandaloneService recommendation, final StandaloneService service, final double threshold) {
        if (!StringUtils.equals(recommendation.getProviderName(), service.getProviderName())) {
            return false;
        }
        final String recommendationServiceDigits = StringUtils.extractDigits(recommendation.getName());
        final String serviceNameDigits = StringUtils.extractDigits(service.getName());
        if (!recommendationServiceDigits.equals(serviceNameDigits)) {
            return false;
        }
        final double longestSubstring = StringUtils.longestSubstring(recommendation.getName(), service.getName());
        return longestSubstring / service.getName().length() > threshold;
    }
    
    @Override
    public void asyncRecommendationRequestName(final List<WeightedRecommender> recommenders, final String name, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter) {
        this.asyncRecommendationRequestName(recommenders, name, context, listener, errorListener, filter, 0.5);
    }
    
    @Override
    public void asyncRecommendationRequestName(final List<WeightedRecommender> recommenders, final String name, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncRecommendationRequestName(recommenders, name, context, listener, errorListener, true);
    }
    
    @Override
    public void asyncRecommendationRequest(final List<WeightedRecommender> recommenders, final StandaloneService service, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter) {
        this.asyncRecommendationRequest(recommenders, service, context, listener, errorListener, filter, 0.5);
    }
    
    @Override
    public void asyncRecommendationRequest(final List<WeightedRecommender> recommenders, final StandaloneService service, final Context context, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncRecommendationRequest(recommenders, service, context, listener, errorListener, true);
    }
    
    @Override
    public void asyncRecommendationRequestByName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncRecommendationRequestByName(name, listener, errorListener, true);
    }
    
    @Override
    public void asyncRecommendationRequestByName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter) {
        this.asyncRecommendationRequestByName(name, listener, errorListener, filter, 0.5);
    }
    
    @Override
    public void asyncRecommendationRequest(final StandaloneService service, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncRecommendationRequest(service, listener, errorListener, true);
    }
    
    @Override
    public void asyncRecommendationRequest(final StandaloneService service, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean filter) {
        this.asyncRecommendationRequest(service, listener, errorListener, filter, 0.5);
    }
    
    static {
        TAG = RecommendationClientImpl.class.getSimpleName();
    }
}
