package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class EsOrQueryBuilderImpl implements EsOrQueryBuilder
{
    @Override
    public ESQuery buildOrQuery(final ESQuery... params) {
        final ESQuery queries = ESQueryBuilders.queryBuilder().appendQueries("queries", params).build();
        return ESQueryBuilders.queryBuilder().appendValue("dis_max", queries).build();
    }
}
