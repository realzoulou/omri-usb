package eu.hradio.httprequestwrapper.exception;

public class NoResultFoundException extends HRadioSearchClientException
{
    private static final String ERROR_PREFIX = "No results for query: ";
    
    public NoResultFoundException(final String queryRoute) {
        super(new Throwable("No results for query: " + queryRoute), ErrorCodes.NO_RESULT_FOUND_EXCEPTION);
    }
}
