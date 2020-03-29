package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.service_search.*;
import eu.hradio.httprequestwrapper.listener.*;
import java.util.*;

public interface ServiceSearchClient extends HRadioHttpClient
{
    void asyncGetAllServices(final OnSearchResultListener<ServiceList> p0, final OnErrorListener p1);
    
    void asyncGetAllEDIServices(final OnSearchResultListener<ServiceList> p0, final OnErrorListener p1);
    
    void asyncServiceSearch(final Map<String, String> p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2);
    
    void asyncServiceSearchByExactName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2);
    
    void asyncServiceSearchByName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2);
    
    void asyncServiceSearch(final Map<String, String> p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3);
    
    void asyncServiceSearchByExactName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3);
    
    void asyncServiceSearchByName(final String p0, final OnSearchResultListener<ServiceList> p1, final OnErrorListener p2, final boolean p3);
}
