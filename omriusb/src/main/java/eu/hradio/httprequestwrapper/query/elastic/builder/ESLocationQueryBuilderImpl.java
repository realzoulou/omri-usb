package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public class ESLocationQueryBuilderImpl implements ESLocationQueryBuilder
{
    private String distance;
    private double lat;
    private double lng;
    
    @Override
    public ESLocationQueryBuilder distance(final String distance) {
        this.distance = distance;
        return this;
    }
    
    @Override
    public ESLocationQueryBuilder location(final double lat, final double lng) {
        this.lat = lat;
        this.lng = lng;
        return this;
    }
    
    @Override
    public ESQuery build() {
        final ESQuery matchAll = ESQueryBuilders.boolQueryBuilder().buildMatchAllQuery();
        final ESQuery geopoint = ESQueryBuilders.queryBuilder().appendValue("lat", this.lat).appendValue("lon", this.lng).build();
        final ESQuery distance = ESQueryBuilders.queryBuilder().appendValue("location.geoPoint", geopoint).appendValue("distance", this.distance).build();
        final ESQuery geoDistance = ESQueryBuilders.queryBuilder().appendValue("geo_distance", distance).build();
        return ESQueryBuilders.nestedQueryBuilder().buildNestedQuery("location", "avg", ESQueryBuilders.queryBuilder().appendQueries("must", matchAll).appendValue("filter", geoDistance).build());
    }
}
