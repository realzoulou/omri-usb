package eu.hradio.httprequestwrapper.query.elastic;

import org.json.*;
import java.util.*;
import eu.hradio.httprequestwrapper.query.elastic.builder.*;

public class ESQuery
{
    private JSONObject object;
    
    public ESQuery() {
        this.object = new JSONObject();
    }
    
    public void put(final String key, final Object value) {
        try {
            this.object.put(key, value);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void put(final String key, final ESQuery value) {
        try {
            this.object.put(key, (Object)value.object);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public void putArray(final String key, final ESQuery[] value) {
        try {
            final JSONArray array = new JSONArray();
            for (int i = 0; i < value.length; ++i) {
                array.put(i, (Object)value[i].object);
            }
            this.object.put(key, (Object)array);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String toString() {
        return this.object.toString();
    }
    
    public static ESQuery emptyQuery() {
        return new ESQuery();
    }
    
    public static ESQuery fromPropertyMap(final Map<String, String> propertyMap) {
        return ESQueryBuilders.fromPropertyMap(propertyMap);
    }
    
    public static ESQuery fromProgrammePropertyMap(final Map<String, String> params) {
        return ESQueryBuilders.fromProgrammePropertyMap(params);
    }
    
    public interface Keys
    {
        public static final String DISTANCE = "distance";
        public static final String NAME = "name";
        public static final String PROVIDER = "providerName";
        public static final String GENRE = "genres";
        public static final String START_TIME = "startTime";
        public static final String HASH = "hash";
        public static final String SERVICE_HASH = "serviceHash";
        public static final String END_TIME = "endTime";
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String COUNTRY_CODE = "location.countryCode";
        public static final String PROGRAMME = "programme";
        public static final String TOP_LEVEL_QUERY = "top-level-query";
        public static final String BEARERS_MIME = "bearers.mimeType";
    }
}
