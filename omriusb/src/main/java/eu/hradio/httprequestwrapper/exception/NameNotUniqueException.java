package eu.hradio.httprequestwrapper.exception;

public class NameNotUniqueException extends HRadioSearchClientException
{
    private static final String ERROR_PREFIX = "No results for query: ";
    
    public NameNotUniqueException(final String queryRoute) {
        super(new Throwable("No results for query: " + queryRoute), ErrorCodes.NO_RESULT_FOUND_EXCEPTION);
    }
}
