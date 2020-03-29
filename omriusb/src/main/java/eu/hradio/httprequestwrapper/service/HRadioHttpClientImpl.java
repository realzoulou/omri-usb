package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.query.*;
import eu.hradio.httprequestwrapper.listener.*;
import java.net.*;
import eu.hradio.httprequestwrapper.exception.*;
import java.io.*;
import org.json.*;
import eu.hradio.httprequestwrapper.parser.*;

public class HRadioHttpClientImpl implements HRadioHttpClient
{
    private final String apiAddress;
    private final String TAG;
    
    public HRadioHttpClientImpl(final String apiAddress) {
        this.TAG = HRadioHttpClientImpl.class.getSimpleName();
        this.apiAddress = apiAddress;
    }
    
    public HRadioHttpClientImpl() {
        this("http://141.84.213.235");
    }
    
    @Override
    public <T> void asyncRequest(final HRadioQuery query, final OnSearchResultListener<T> listener, final OnErrorListener errorListener, final Class<T> expectedResultType) {
        new AsyncRequestTask((OnSearchResultListener<T>)listener, errorListener, (SearchDelegate<HRadioQuery, T>)this::blockingRequest, (Class<T>)expectedResultType)
                .execute((Object[])new HRadioQuery[] { query });
    }
    
    @Override
    public <T> T blockingRequest(final HRadioQuery query, final Class<T> expectedResultType) throws JsonDecoderTypeMismatch, NoResultFoundException, NetworkException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(this.apiAddress + query.toUrlString()).openConnection();
            connection.setRequestMethod(query.getRequestMethod());
            if (query.hasBody()) {
                connection.setRequestProperty("Content-Type", "application/json");
                final byte[] body = query.getBody().getBytes("UTF-8");
                final OutputStream outputStream = connection.getOutputStream();
                outputStream.write(body);
                outputStream.close();
            }
            return this.readResponse(new BufferedInputStream(connection.getInputStream()), query, expectedResultType);
        }
        catch (IOException e) {
            throw new NetworkException(e.getCause());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private <T> T readResponse(final BufferedInputStream in, final HRadioQuery query, final Class<T> expectedResultType) throws NoResultFoundException, JsonDecoderTypeMismatch, NetworkException {
        final StringBuilder builder = new StringBuilder();
        final char[] buffer = new char[1024];
        try {
            final InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, charsRead);
            }
            in.close();
            final String response = builder.toString();
            if (response.isEmpty()) {
                throw new NoResultFoundException(query.toUrlString() + query.getBody());
            }
            return this.parseResponse(new JSONObject(response), expectedResultType);
        }
        catch (JSONException e) {
            throw new JsonDecoderTypeMismatch(e.getMessage(), e.getCause());
        }
        catch (IOException e2) {
            throw new NetworkException(e2);
        }
    }
    
    private <T> T parseResponse(final JSONObject response, final Class<T> type) throws JsonDecoderTypeMismatch {
        final JsonParser parser = new JsonParser();
        return parser.parse(type, response);
    }
}
