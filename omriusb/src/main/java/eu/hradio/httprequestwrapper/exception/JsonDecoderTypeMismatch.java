package eu.hradio.httprequestwrapper.exception;

public class JsonDecoderTypeMismatch extends HRadioSearchClientException
{
    private String jsonString;
    
    public JsonDecoderTypeMismatch(final String jsonString, final Throwable t) {
        super(t, ErrorCodes.JSON_DECODER_EXCEPTION);
        this.jsonString = jsonString;
    }
    
    public String getJsonString() {
        return this.jsonString;
    }
}
