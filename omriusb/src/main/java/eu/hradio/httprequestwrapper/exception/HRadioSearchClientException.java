package eu.hradio.httprequestwrapper.exception;

public class HRadioSearchClientException extends Exception
{
    private ErrorCodes errorCode;
    
    public HRadioSearchClientException(final Throwable t, final ErrorCodes errorCode) {
        super(t);
        this.errorCode = errorCode;
    }
    
    public ErrorCodes getErrorCode() {
        return this.errorCode;
    }
    
    public void setErrorCode(final ErrorCodes errorCode) {
        this.errorCode = errorCode;
    }
}
