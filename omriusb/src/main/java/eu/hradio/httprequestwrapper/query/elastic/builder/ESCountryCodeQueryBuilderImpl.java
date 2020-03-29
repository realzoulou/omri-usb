package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESCountryCodeQueryBuilderImpl extends ESQueryBuilderImpl implements ESCountryCodeQueryBuilder
{
    private String countryCode;
    
    @Override
    public ESCountryCodeQueryBuilder countryCode(final String countryCode) {
        this.countryCode = countryCode;
        return this;
    }
    
    @Override
    public ESQuery build() {
        return ESQueryBuilders.nestedQueryBuilder().buildNestedQuery("location", "avg", ESQueryBuilders.queryBuilder().appendQueries("must", ESQueryBuilders.boolQueryBuilder().buildMatchQuery(ESQueryBuilders.queryBuilder().appendValue("location.countryCode", this.countryCode).build())).build());
    }
}
