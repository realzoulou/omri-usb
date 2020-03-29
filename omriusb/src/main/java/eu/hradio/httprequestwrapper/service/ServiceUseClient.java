package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.service_use.*;
import eu.hradio.httprequestwrapper.listener.*;

public interface ServiceUseClient extends HRadioHttpClient
{
    void asyncServiceUseRequest(final ServiceUse p0, final OnSearchResultListener<ServiceUseResult> p1, final OnErrorListener p2);
}
