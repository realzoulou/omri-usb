package eu.hradio.httprequestwrapper.dtos.service_search;

enum MediaDescriptionType
{
    MULTIMEDIA, 
    DESCRIPTION;
    
    public static MediaDescriptionType fromString(final String type) {
        for (final MediaDescriptionType t : values()) {
            if (t.name().toLowerCase().equals(type.toLowerCase())) {
                return t;
            }
        }
        return null;
    }
}
