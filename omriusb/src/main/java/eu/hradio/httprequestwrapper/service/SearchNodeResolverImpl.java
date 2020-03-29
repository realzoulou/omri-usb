package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.*;
import eu.hradio.httprequestwrapper.listener.*;
import java.net.*;
import java.io.*;
import eu.hradio.httprequestwrapper.parser.*;
import org.json.*;
import eu.hradio.httprequestwrapper.exception.*;

public class SearchNodeResolverImpl implements SearchNodeResolver
{
    private static final String indexEndpoint = "/index";
    private static final String indexEndpointSelf = "/index?self";
    private static final String defaultAddress = "http://141.84.213.235:8080/api/v1";
    
    public void resolveNodeLocation(final OnSearchResultListener<SearchNode[]> nodeResultListener, final OnErrorListener errorListener) {
        new AsyncRequestTask(nodeResultListener, errorListener, (input, type) -> this.blockingRequest(input + "/index?self"), SearchNode[].class).execute("http://141.84.213.235:8080/api/v1");
    }
    
    @Override
    public void resolveNodeLocation(final String nodeUrl, final OnSearchResultListener<SearchNode[]> nodeResultListener, final OnErrorListener errorListener) {
        new AsyncRequestTask(nodeResultListener, errorListener, (input, type) -> this.blockingRequest(input + "/index?self"), SearchNode[].class).execute(nodeUrl);
    }
    
    @Override
    public void getAllNodes(final String nodeUrl, final OnSearchResultListener<SearchNode[]> nodeResultListener, final OnErrorListener errorListener) {
        new AsyncRequestTask(nodeResultListener, errorListener, (input, type) -> this.blockingRequest(input + "/index"), SearchNode[].class).execute(nodeUrl);
    }
    
    private SearchNode[] blockingRequest(final String nodeUrl) throws NetworkException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(nodeUrl).openConnection();
            return this.readResponse(new BufferedInputStream(connection.getInputStream()), nodeUrl);
        }
        catch (IOException e) {
            throw new NetworkException(e.getCause());
        }
        catch (JsonDecoderTypeMismatch e2) {
            throw new NetworkException(e2.getCause());
        }
        catch (NoResultFoundException e3) {
            throw new NetworkException(e3.getCause());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private SearchNode[] readResponse(final BufferedInputStream in, final String nodeUrl) throws NoResultFoundException, JsonDecoderTypeMismatch, NetworkException {
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
                throw new NoResultFoundException(nodeUrl);
            }
            final JSONArray jsonArray = new JSONArray(response);
            final SearchNode[] searchNodes = new SearchNode[jsonArray.length()];
            final JsonParser parser = new JsonParser();
            for (int i = 0; i < searchNodes.length; ++i) {
                searchNodes[i] = parser.parse(SearchNode.class, jsonArray.getJSONObject(i));
            }
            return searchNodes;
        }
        catch (JSONException e) {
            throw new JsonDecoderTypeMismatch(e.getMessage(), e.getCause());
        }
        catch (IOException e2) {
            throw new NetworkException(e2);
        }
    }
}
