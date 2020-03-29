package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface ESLocationQueryBuilder
{
    ESLocationQueryBuilder distance(final String p0);
    
    ESLocationQueryBuilder location(final double p0, final double p1);
    
    ESQuery build();
}
