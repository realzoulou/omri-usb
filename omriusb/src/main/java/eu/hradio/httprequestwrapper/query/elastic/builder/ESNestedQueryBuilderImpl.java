package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESNestedQueryBuilderImpl implements ESNestedQueryBuilder
{
    @Override
    public ESQuery buildNestedQuery(final String path, final String scoreMode, final ESQuery params) {
        final ESQuery bool = new ESQuery();
        bool.put("bool", params);
        final ESQuery query = new ESQuery();
        query.put("path", path);
        query.put("score_mode", scoreMode);
        query.put("query", bool);
        final ESQuery nested = new ESQuery();
        nested.put("nested", query);
        return nested;
    }
}
