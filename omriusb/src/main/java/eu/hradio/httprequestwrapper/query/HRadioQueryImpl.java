package eu.hradio.httprequestwrapper.query;

import eu.hradio.httprequestwrapper.util.*;
import java.util.*;

public class HRadioQueryImpl implements HRadioQuery
{
    private int port;
    private String method;
    private Map<String, String> properties;
    private Set<String> endPoints;
    private String body;
    
    public HRadioQueryImpl() {
        this.port = 8080;
        this.method = "GET";
        this.properties = new HashMap<String, String>();
        this.endPoints = new LinkedHashSet<String>();
        this.body = "";
        this.addEndPoint("api");
        this.addEndPoint("v1");
    }
    
    @Override
    public void append(String key, String value) {
        key = HRadioUrlEncoder.encode(key);
        value = HRadioUrlEncoder.encode(value.trim());
        this.properties.put(key, value);
    }
    
    @Override
    public void addEndPoint(final String key) {
        this.endPoints.add(key);
    }
    
    @Override
    public void setBody(final String body) {
        this.body = body;
    }
    
    @Override
    public String toUrlString() {
        final StringBuilder builder = new StringBuilder(":" + this.port + "/");
        final Iterator<String> endPointIterator = this.endPoints.iterator();
        while (endPointIterator.hasNext()) {
            final String endPoint = endPointIterator.next();
            builder.append(endPoint);
            if (endPointIterator.hasNext()) {
                builder.append("/");
            }
        }
        if (!this.properties.isEmpty()) {
            builder.append("?");
        }
        final Iterator<Map.Entry<String, String>> propertyIterator = this.properties.entrySet().iterator();
        while (propertyIterator.hasNext()) {
            final Map.Entry<String, String> entry = propertyIterator.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            if (propertyIterator.hasNext()) {
                builder.append("&");
            }
        }
        return builder.toString();
    }
    
    @Override
    public String getBody() {
        return this.body;
    }
    
    @Override
    public boolean hasBody() {
        return !this.body.isEmpty();
    }
    
    @Override
    public void setPort(final int port) {
        this.port = port;
    }
    
    @Override
    public void setRequestMethod(final String method) {
        this.method = method;
    }
    
    @Override
    public String getRequestMethod() {
        return this.method;
    }
}
