package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface ESBoolQueryBuilder
{
    ESQuery buildMatchAllQuery();
    
    ESQuery buildMatchQuery(final ESQuery p0);
    
    ESQuery buildWildcardQuery(final ESQuery p0);
}
