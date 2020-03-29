package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.*;
import eu.hradio.httprequestwrapper.listener.*;

public interface SearchNodeResolver
{
    void resolveNodeLocation(final String p0, final OnSearchResultListener<SearchNode[]> p1, final OnErrorListener p2);
    
    void getAllNodes(final String p0, final OnSearchResultListener<SearchNode[]> p1, final OnErrorListener p2);
}
