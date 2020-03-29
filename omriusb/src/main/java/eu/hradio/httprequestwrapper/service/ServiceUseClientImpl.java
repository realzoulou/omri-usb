package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.service_use.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.parser.*;
import eu.hradio.httprequestwrapper.exception.*;
import eu.hradio.httprequestwrapper.query.*;

public class ServiceUseClientImpl extends HRadioHttpClientImpl implements ServiceUseClient
{
    @Override
    public void asyncServiceUseRequest(final ServiceUse serviceUse, final OnSearchResultListener<ServiceUseResult> listener, final OnErrorListener errorListener) {
        final HRadioQuery serviceUseQuery = new HRadioQueryImpl();
        serviceUseQuery.setPort(8110);
        serviceUseQuery.addEndPoint("service_use");
        serviceUseQuery.setRequestMethod("POST");
        final JsonParser parser = new JsonParser();
        try {
            final String serviceUseBody = parser.toJSON(serviceUse).toString();
            serviceUseQuery.setBody(serviceUseBody);
            this.asyncRequest(serviceUseQuery, listener, errorListener, ServiceUseResult.class);
        }
        catch (JsonEncoderTypeMismatch jsonEncoderTypeMismatch) {
            errorListener.onError(jsonEncoderTypeMismatch);
        }
    }
}
