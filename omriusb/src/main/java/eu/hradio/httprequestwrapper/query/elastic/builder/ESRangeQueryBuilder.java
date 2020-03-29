package eu.hradio.httprequestwrapper.query.elastic.builder;

public interface ESRangeQueryBuilder extends ESQueryBuilder
{
    ESRangeQueryBuilder from(final Object p0);
    
    ESRangeQueryBuilder to(final Object p0);
}
