package eu.hradio.httprequestwrapper.exception;

public class JsonEncoderTypeMismatch extends HRadioSearchClientException
{
    private Object object;
    
    public JsonEncoderTypeMismatch(final Object object, final Throwable t) {
        super(t, ErrorCodes.JSON_ENCODER_EXCEPTION);
        this.object = object;
    }
    
    public Object getObject() {
        return this.object;
    }
}
