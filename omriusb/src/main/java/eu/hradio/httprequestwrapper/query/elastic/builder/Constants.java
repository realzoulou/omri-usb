package eu.hradio.httprequestwrapper.query.elastic.builder;

public interface Constants
{
    public interface Values
    {
        public static final String SYNTAX_ELASTIC_COMPLEX = "ELASTIC_COMPLEX";
        public static final String PATH_LOCATION = "location";
        public static final String SCORE_MODE_AVG = "avg";
        public static final String SEARCH_FEDERATION_MODE_FEDERATED = "FEDERATED";
    }
    
    public interface TlqKeys
    {
        public static final String MAX_BREATH = "maxBreadth";
        public static final String MAX_DEPTH = "maxDepth";
        public static final String TIMEOUT = "timeout";
        public static final String SEARCH_FEDERATION_MODE = "searchFederationMode";
        public static final String ID = "id_tlq";
        public static final String GENRES = "genres_tlq";
        public static final String KEYWORDS = "keywords_tlq";
    }
    
    public interface Keys
    {
        public static final String WILDCARD = "wildcard";
        public static final String FILTER = "filter";
        public static final String RANGE = "range";
        public static final String GTE = "gte";
        public static final String LTE = "lte";
        public static final String BOOL = "bool";
        public static final String MUST = "must";
        public static final String MATCH = "match";
        public static final String DIS_MAX = "dis_max";
        public static final String QUERIES = "queries";
        public static final String MATCH_ALL = "match_all";
        public static final String QUERY = "query";
        public static final String PATH = "path";
        public static final String SCORE_MODE = "score_mode";
        public static final String NESTED = "nested";
        public static final String SYNTAX = "syntax";
        public static final String LOCAL_QUERY = "localQuery";
        public static final String TOP_LEVEL_QUERY = "topLevelQuery";
        public static final String GEO_DISTANCE = "geo_distance";
        public static final String GEOPOINT = "location.geoPoint";
        public static final String GENRES = "genres";
        public static final String KEYWORDS = "keywords";
    }
}
