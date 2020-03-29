package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;
import java.util.*;

public final class ESQueryBuilders
{
    public static ESQueryBuilder queryBuilder() {
        return new ESQueryBuilderImpl();
    }
    
    public static ESRangeQueryBuilder rangeQueryBuilder(final String propertyKey) {
        return new ESRangeQueryBuilderImpl(propertyKey);
    }
    
    public static ESNestedQueryBuilder nestedQueryBuilder() {
        return new ESNestedQueryBuilderImpl();
    }
    
    public static ESBoolQueryBuilder boolQueryBuilder() {
        return new ESBoolQueryBuilderImpl();
    }
    
    public static ESLocalQueryBuilder localQueryBuilder() {
        return new ESLocalQueryBuilderImpl();
    }
    
    public static ESLocationQueryBuilder locationQueryBuilder() {
        return new ESLocationQueryBuilderImpl();
    }
    
    public static ESCountryCodeQueryBuilder countryCodeQueryBuilder() {
        return new ESCountryCodeQueryBuilderImpl();
    }
    
    public static ESTopLevelBuilder topLevelQueryBuilder() {
        return new ESTopLevelBuilderImpl();
    }
    
    public static EsOrQueryBuilder orQueryBuilder() {
        return new EsOrQueryBuilderImpl();
    }
    
    public static ESQuery fromPropertyMap(final Map<String, String> propertyMap) {
        if (propertyMap.containsKey("searchFederationMode")) {
            final String searchFedMod = propertyMap.remove("searchFederationMode");
            final String tlqGenre = propertyMap.remove("genres_tlq");
            final String tlqKeywords = propertyMap.remove("keywords_tlq");
            final String tlqBreath = propertyMap.remove("maxBreadth");
            final String tlqDepth = propertyMap.remove("maxDepth");
            final String tlqTimeout = propertyMap.remove("timeout");
            final String tlqID = propertyMap.remove("id_tlq");
            final int breath = (tlqBreath == null) ? 2 : Integer.parseInt(tlqBreath);
            final int depth = (tlqDepth == null) ? 2 : Integer.parseInt(tlqDepth);
            final long timout = (tlqTimeout == null) ? 1000L : Long.parseLong(tlqTimeout);
            final ESQuery local = localQueryFromMap(propertyMap);
            final ESTopLevelBuilder builder = topLevelQueryBuilder().appendTopParam("maxBreadth", breath).appendTopParam("maxDepth", depth).appendTopParam("timeout", timout).appendTopParam("searchFederationMode", searchFedMod);
            if (tlqID != null) {
                builder.appendTopParam("id", Integer.parseInt(tlqID));
            }
            ESQuery genre = null;
            ESQuery keywords = null;
            if (tlqGenre != null) {
                genre = boolQueryBuilder().buildMatchQuery(queryBuilder().appendValue("genres", tlqGenre).build());
            }
            if (tlqKeywords != null) {
                keywords = boolQueryBuilder().buildMatchQuery(queryBuilder().appendValue("keywords", tlqKeywords).build());
            }
            ESQuery mustMatch;
            if (genre != null && keywords != null) {
                mustMatch = queryBuilder().appendQueries("must", genre, keywords).build();
            }
            else if (genre != null) {
                mustMatch = queryBuilder().appendValue("must", genre).build();
            }
            else if (keywords != null) {
                mustMatch = queryBuilder().appendValue("must", keywords).build();
            }
            else {
                mustMatch = boolQueryBuilder().buildMatchAllQuery();
            }
            builder.appendValue("bool", mustMatch);
            return builder.setLocalQuery(local).build();
        }
        return localQueryFromMap(propertyMap);
    }
    
    private static ESQuery localQueryFromMap(final Map<String, String> propertyMap) {
        ESQuery query = null;
        if (propertyMap.containsKey("distance") && propertyMap.containsKey("lat") && propertyMap.containsKey("lon")) {
            final String distanceString = propertyMap.remove("distance").trim();
            final double lat = Double.parseDouble(propertyMap.remove("lat"));
            final double lng = Double.parseDouble(propertyMap.remove("lon"));
            query = locationQueryBuilder().distance(distanceString).location(lat, lng).build();
        }
        int size = (query == null) ? propertyMap.size() : (propertyMap.size() + 1);
        size = (propertyMap.containsKey("endTime") ? (size - 1) : size);
        final ESQuery[] matchParams = new ESQuery[size];
        int i = 0;
        if (query != null) {
            matchParams[i++] = query;
        }
        for (final Map.Entry<String, String> entry : propertyMap.entrySet()) {
            if (!entry.getKey().equals("endTime") && !entry.getKey().equals("distance") && !entry.getKey().equals("lon")) {
                if (entry.getKey().equals("lon")) {
                    continue;
                }
                ESQuery param;
                if (entry.getKey().contains(".")) {
                    final String path = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
                    param = nestedQueryBuilder().buildNestedQuery(path, "avg", queryBuilder().appendQueries("must", boolQueryBuilder().buildMatchQuery(queryBuilder().appendValue(entry.getKey(), entry.getValue()).build())).build());
                }
                else if (entry.getKey().equals("startTime")) {
                    param = rangeQueryBuilder("startTime").from(propertyMap.get("startTime")).to(propertyMap.get("endTime")).build();
                }
                else {
                    final String[] values = entry.getValue().split(",");
                    if (values.length > 1) {
                        final ESQuery[] orQueries = new ESQuery[values.length];
                        int k = 0;
                        for (final String value : values) {
                            if (value.contains("*")) {
                                final ESQuery entryQuery = queryBuilder().appendValue(entry.getKey(), value.trim()).build();
                                orQueries[k++] = boolQueryBuilder().buildWildcardQuery(entryQuery);
                            }
                            else {
                                final ESQuery entryQuery = queryBuilder().appendValue(entry.getKey(), value.trim()).build();
                                orQueries[k++] = boolQueryBuilder().buildMatchQuery(entryQuery);
                            }
                        }
                        param = orQueryBuilder().buildOrQuery(orQueries);
                    }
                    else if (entry.getValue().contains("*")) {
                        final ESQuery entryQuery2 = queryBuilder().appendValue(entry.getKey(), entry.getValue().trim()).build();
                        param = boolQueryBuilder().buildWildcardQuery(entryQuery2);
                    }
                    else {
                        final ESQuery entryQuery2 = queryBuilder().appendValue(entry.getKey(), entry.getValue().trim()).build();
                        param = boolQueryBuilder().buildMatchQuery(entryQuery2);
                    }
                }
                matchParams[i++] = param;
            }
        }
        return localQueryBuilder().buildLocalQuery(queryBuilder().appendValue("bool", queryBuilder().appendQueries("must", matchParams).build()).build());
    }
    
    public static ESQuery fromProgrammePropertyMap(final Map<String, String> params) {
        final ESQuery[] matchParams = new ESQuery[params.size()];
        int i = 0;
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            final ESQuery entryQuery = queryBuilder().appendValue(entry.getKey(), entry.getValue().trim()).build();
            final ESQuery param = boolQueryBuilder().buildMatchQuery(entryQuery);
            matchParams[i++] = param;
        }
        final ESQuery query = orQueryBuilder().buildOrQuery(matchParams);
        return localQueryBuilder().buildLocalQuery(queryBuilder().appendValue("bool", queryBuilder().appendQueries("must", query).build()).build());
    }
}
