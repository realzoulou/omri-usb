package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface ESNestedQueryBuilder
{
    ESQuery buildNestedQuery(final String p0, final String p1, final ESQuery p2);
}
