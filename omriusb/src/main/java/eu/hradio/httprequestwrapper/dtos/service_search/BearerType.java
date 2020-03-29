package eu.hradio.httprequestwrapper.dtos.service_search;

public enum BearerType
{
    DAB, 
    HTTP, 
    FM, 
    DRM;
    
    public static BearerType fromString(final String type) {
        for (final BearerType t : values()) {
            if (t.name().toLowerCase().equals(type.toLowerCase())) {
                return t;
            }
        }
        return null;
    }
}
