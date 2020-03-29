package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface EsOrQueryBuilder
{
    ESQuery buildOrQuery(final ESQuery... p0);
}
