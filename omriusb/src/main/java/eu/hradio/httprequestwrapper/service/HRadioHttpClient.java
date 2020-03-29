package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.query.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.exception.*;

public interface HRadioHttpClient
{
    public static final String DEFAULT_ADDRESS = "http://141.84.213.235";
    
     <T> void asyncRequest(final HRadioQuery p0, final OnSearchResultListener<T> p1, final OnErrorListener p2, final Class<T> p3);
    
     <T> T blockingRequest(final HRadioQuery p0, final Class<T> p1) throws NetworkException, JsonDecoderTypeMismatch, NoResultFoundException;
}
