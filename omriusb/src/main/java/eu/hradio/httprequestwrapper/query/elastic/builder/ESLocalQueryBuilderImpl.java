package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESLocalQueryBuilderImpl implements ESLocalQueryBuilder
{
    @Override
    public ESQuery buildLocalQuery(final ESQuery query) {
        final ESQuery localQuery = new ESQuery();
        localQuery.put("syntax", "ELASTIC_COMPLEX");
        localQuery.put("query", query);
        final ESQuery res = new ESQuery();
        res.put("localQuery", localQuery);
        return res;
    }
}
