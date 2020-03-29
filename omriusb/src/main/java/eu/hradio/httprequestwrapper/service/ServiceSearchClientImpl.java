package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.query.*;
import eu.hradio.httprequestwrapper.query.elastic.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;
import java.util.*;
import eu.hradio.httprequestwrapper.dtos.programme.*;

public class ServiceSearchClientImpl extends HRadioHttpClientImpl implements ServiceSearchClient
{
    private static final double PROGRAMME_RANK_THRESHOLD = 0.8;
    
    @Override
    public void asyncGetAllServices(final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        final HRadioQuery query = new HRadioQueryImpl();
        query.addEndPoint("services");
        query.append("q", "name:*");
        query.setRequestMethod("GET");
        this.crawlAllPages(query, listener, errorListener, new ArrayList<ServiceList>());
    }
    
    @Override
    public void asyncGetAllEDIServices(final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        final Map<String, String> builder = new HashMap<String, String>();
        builder.put("bearers.mimeType", "*edi");
        final String esQuery = ESQuery.fromPropertyMap(builder).toString();
        final HRadioQuery hQuery = new HRadioQueryImpl();
        hQuery.addEndPoint("services");
        hQuery.setRequestMethod("POST");
        hQuery.setBody(esQuery);
        this.crawlAllPages(hQuery, listener, errorListener, new ArrayList<ServiceList>());
    }
    
    @Override
    public void asyncServiceSearch(final Map<String, String> params, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean returnALl) {
        final String programmeQuery = params.remove("programme");
        if (programmeQuery == null || programmeQuery.isEmpty()) {
            this.asyncServiceSearchHelper(params, listener, errorListener, returnALl);
        }
        else {
            this.asyncServiceSearchWithProgramme(programmeQuery, params, listener, errorListener, returnALl);
        }
    }
    
    private void asyncServiceSearchWithProgramme(final String programmeQuery, final Map<String, String> params, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean returnALl) {
        final Map<String, String> progParams = new HashMap<String, String>();
        progParams.put("longName", programmeQuery);
        progParams.put("name", programmeQuery);
        progParams.put("description", programmeQuery);
        new MetaDataSearchClientImpl().asyncProgrammeSearch(progParams, res -> {
            final Set<String> serviceHashes = new HashSet<String>();
            final StringBuilder hashBuilder = new StringBuilder();
            double rank = 0.0;
            String first;
            if (res.getNumberOfElements() > 0) {
                first = res.getContent()[0].getProgramme().getServiceHash();
                serviceHashes.add(first);
                hashBuilder.append(first);
                rank = res.getContent()[0].getScore();
            }
            final RankedStandaloneProgramme[] array = res.getContent();
            final int length = array.length;
            int i = 0;
            while (i < length) {
                RankedStandaloneProgramme programme = array[i];
                if (programme.getScore() < rank * 0.8) {
                    break;
                }
                else {
                    String hash = programme.getProgramme().getServiceHash();
                    if (!serviceHashes.contains(hash)) {
                        hashBuilder.append(",").append(hash);
                        serviceHashes.add(hash);
                    }
                    ++i;
                }
            }
            params.put("hash", hashBuilder.toString());
            this.asyncServiceSearchHelper(params, listener, errorListener, returnALl);
        }, errorListener, false);
    }
    
    private void asyncServiceSearchHelper(final Map<String, String> params, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean returnALl) {
        final String tlq = params.remove("top-level-query");
        final ESQuery bodyQuery = ESQuery.fromPropertyMap(params);
        final HRadioQuery query = new HRadioQueryImpl();
        if (tlq != null && !tlq.isEmpty()) {
            query.append("tlq", tlq);
        }
        query.addEndPoint("services");
        query.setBody(bodyQuery.toString());
        query.setRequestMethod("POST");
        if (returnALl) {
            this.crawlAllPages(query, listener, errorListener, new ArrayList<ServiceList>());
        }
        else {
            this.asyncRequest(query, listener, errorListener, ServiceList.class);
        }
    }
    
    @Override
    public void asyncServiceSearchByExactName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean returnALl) {
        final HRadioQuery query = new HRadioQueryImpl();
        query.addEndPoint("services");
        query.append("q", "name:\"" + name + "\"");
        query.setRequestMethod("GET");
        if (returnALl) {
            this.crawlAllPages(query, listener, errorListener, new ArrayList<ServiceList>());
        }
        else {
            this.asyncRequest(query, listener, errorListener, ServiceList.class);
        }
    }
    
    @Override
    public void asyncServiceSearchByName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final boolean returnALl) {
        final HRadioQuery query = new HRadioQueryImpl();
        query.addEndPoint("services");
        query.append("q", "name:" + name + "*");
        query.setRequestMethod("GET");
        if (returnALl) {
            this.crawlAllPages(query, listener, errorListener, new ArrayList<ServiceList>());
        }
        else {
            this.asyncRequest(query, listener, errorListener, ServiceList.class);
        }
    }
    
    @Override
    public void asyncServiceSearch(final Map<String, String> params, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncServiceSearch(params, listener, errorListener, false);
    }
    
    @Override
    public void asyncServiceSearchByExactName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncServiceSearchByExactName(name, listener, errorListener, false);
    }
    
    @Override
    public void asyncServiceSearchByName(final String name, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener) {
        this.asyncServiceSearchByName(name, listener, errorListener, false);
    }
    
    private void crawlAllPages(final HRadioQuery query, final OnSearchResultListener<ServiceList> listener, final OnErrorListener errorListener, final List<ServiceList> pageList) {
        query.append("page", pageList.size() + "");
        query.append("size", "1000");
        this.asyncRequest(query, list -> {
            pageList.add(list);
            if (list.getNumberOfElements() == list.getSize()) {
                this.crawlAllPages(query, listener, errorListener, pageList);
            }
            else {
                List<RankedStandaloneService> mergedPages = new ArrayList<RankedStandaloneService>();
                final Iterator<ServiceList> iterator = pageList.iterator();
                while (iterator.hasNext()) {
                    ServiceList programmeList = iterator.next();
                    mergedPages.addAll(Arrays.asList(programmeList.getContent()));
                }
                list.setContent(mergedPages.toArray(new RankedStandaloneService[0]));
                listener.onResult(list);
            }
        }, errorListener, ServiceList.class);
    }
}
