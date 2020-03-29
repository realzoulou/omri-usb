package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESBoolQueryBuilderImpl implements ESBoolQueryBuilder
{
    @Override
    public ESQuery buildMatchAllQuery() {
        final ESQuery query = ESQueryBuilders.queryBuilder().appendValue("match_all", ESQuery.emptyQuery()).build();
        return ESQueryBuilders.queryBuilder().appendValue("must", query).build();
    }
    
    @Override
    public ESQuery buildMatchQuery(final ESQuery params) {
        return ESQueryBuilders.queryBuilder().appendValue("match", params).build();
    }
    
    @Override
    public ESQuery buildWildcardQuery(final ESQuery params) {
        return ESQueryBuilders.queryBuilder().appendValue("wildcard", params).build();
    }
}
