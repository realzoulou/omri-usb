package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESRangeQueryBuilderImpl extends ESQueryBuilderImpl implements ESRangeQueryBuilder
{
    private Object from;
    private Object to;
    private String propertyKey;
    
    ESRangeQueryBuilderImpl(final String propertyKey) {
        this.propertyKey = propertyKey;
    }
    
    @Override
    public ESQuery build() {
        final ESQuery range = ESQuery.emptyQuery();
        range.put("lte", this.to);
        range.put("gte", this.from);
        this.params.put(this.propertyKey, range);
        return ESQueryBuilders.queryBuilder().appendValue("range", this.params).build();
    }
    
    @Override
    public ESRangeQueryBuilder from(final Object o) {
        this.from = o;
        return this;
    }
    
    @Override
    public ESRangeQueryBuilder to(final Object o) {
        this.to = o;
        return this;
    }
}
