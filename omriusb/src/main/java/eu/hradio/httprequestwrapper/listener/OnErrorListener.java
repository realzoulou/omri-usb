package eu.hradio.httprequestwrapper.listener;

import eu.hradio.httprequestwrapper.exception.*;

@FunctionalInterface
public interface OnErrorListener
{
    void onError(final HRadioSearchClientException p0);
}
