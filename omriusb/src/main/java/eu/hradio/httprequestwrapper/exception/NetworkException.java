package eu.hradio.httprequestwrapper.exception;

public class NetworkException extends HRadioSearchClientException
{
    public NetworkException(final Throwable t) {
        super(t, ErrorCodes.NETWORK_EXCEPTION);
    }
}
