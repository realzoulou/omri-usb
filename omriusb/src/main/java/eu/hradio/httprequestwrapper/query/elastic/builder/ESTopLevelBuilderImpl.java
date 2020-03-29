package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;
import java.util.*;

public class ESTopLevelBuilderImpl extends ESQueryBuilderImpl implements ESTopLevelBuilder
{
    private Map<String, Object> topParams;
    private ESQuery localQuery;
    
    public ESTopLevelBuilderImpl() {
        this.topParams = new HashMap<String, Object>();
    }
    
    @Override
    public ESTopLevelBuilderImpl appendValue(final String key, final Object value) {
        super.appendValue(key, value);
        return this;
    }
    
    @Override
    public ESTopLevelBuilderImpl appendValue(final String key, final ESQuery value) {
        super.appendValue(key, value);
        return this;
    }
    
    @Override
    public ESTopLevelBuilderImpl appendQueries(final String key, final ESQuery... value) {
        super.appendQueries(key, value);
        return this;
    }
    
    @Override
    public ESTopLevelBuilder setLocalQuery(final ESQuery local) {
        this.localQuery = local;
        return this;
    }
    
    @Override
    public ESTopLevelBuilder appendTopParam(final String key, final Object value) {
        this.topParams.put(key, value);
        return this;
    }
    
    @Override
    public ESQuery build() {
        final ESQuery result = (this.localQuery != null) ? this.localQuery : ESQuery.emptyQuery();
        final ESQuery query = super.build();
        final ESQuery tlQuery = new ESQuery();
        tlQuery.put("syntax", "ELASTIC_COMPLEX");
        tlQuery.put("query", query);
        result.put("topLevelQuery", tlQuery);
        for (final String key : this.topParams.keySet()) {
            result.put(key, this.topParams.get(key));
        }
        return result;
    }
}
