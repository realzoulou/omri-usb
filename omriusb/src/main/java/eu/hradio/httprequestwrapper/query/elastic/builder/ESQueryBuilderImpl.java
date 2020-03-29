package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESQueryBuilderImpl implements ESQueryBuilder
{
    protected ESQuery params;
    
    public ESQueryBuilderImpl() {
        this.params = new ESQuery();
    }
    
    @Override
    public ESQueryBuilder appendValue(final String key, final Object value) {
        this.params.put(key, value);
        return this;
    }
    
    @Override
    public ESQueryBuilder appendValue(final String key, final ESQuery value) {
        this.params.put(key, value);
        return this;
    }
    
    @Override
    public ESQueryBuilder appendQueries(final String key, final ESQuery... value) {
        this.params.putArray(key, value);
        return this;
    }
    
    @Override
    public ESQuery build() {
        final ESQuery res = this.params;
        this.params = new ESQuery();
        return res;
    }
    
    @Override
    public String toString() {
        return this.params.toString();
    }
}
