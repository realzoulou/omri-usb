package eu.hradio.httprequestwrapper.query;

public interface HRadioQuery
{
    void append(final String p0, final String p1);
    
    void addEndPoint(final String p0);
    
    void setBody(final String p0);
    
    String toUrlString();
    
    String getBody();
    
    boolean hasBody();
    
    void setPort(final int p0);
    
    void setRequestMethod(final String p0);
    
    String getRequestMethod();
    
    public interface HttpMethods
    {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
    }
    
    public interface Ports
    {
        public static final int META_DATA_PORT = 8090;
        public static final int SERVICE_USE_PORT = 8110;
        public static final int USER_REPORT_PORT = 8110;
        public static final int RECOMMENDATION_PORT = 8100;
        public static final int SEARCHPORT = 8080;
    }
    
    public interface EndPoints
    {
        public static final String SEARCH = "services";
        public static final String RECOMMENDATION = "recommendations";
        public static final String SERVICE_USE = "service_use";
        public static final String RECOMMENDER_STATS = "recommender_stats";
        public static final String PROGRAMMES = "programmes";
        public static final String USER_REPORT = "user_reports";
    }
}
