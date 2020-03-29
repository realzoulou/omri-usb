package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface ESQueryBuilder
{
    ESQuery build();
    
    ESQueryBuilder appendValue(final String p0, final Object p1);
    
    ESQueryBuilder appendValue(final String p0, final ESQuery p1);
    
    ESQueryBuilder appendQueries(final String p0, final ESQuery... p1);
}
