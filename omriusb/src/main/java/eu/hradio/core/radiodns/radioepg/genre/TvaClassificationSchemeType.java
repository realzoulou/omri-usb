package eu.hradio.core.radiodns.radioepg.genre;

public enum TvaClassificationSchemeType
{
    TVA_CS_UNKNOWN(""), 
    TVA_CS_INTENTION("IntentionCS"), 
    TVA_CS_FORMAT("FormatCS"), 
    TVA_CS_CONTENT("ContentCS"), 
    TVA_CS_ORIGINATION("OriginationCS"), 
    TVA_CS_INTENDED_AUDIENCE("IntendedAudienceCS"), 
    TVA_CS_CONTENT_ALERT("ContentAlertCS"), 
    TVA_CS_MEDIA_TYPE("MediaTypeCS"), 
    TVA_CS_ATMOSPHERE("AtmosphereCS");
    
    private final String mScheme;
    
    private TvaClassificationSchemeType(final String classScheme) {
        this.mScheme = classScheme;
    }
    
    public final String getScheme() {
        return this.mScheme;
    }
    
    public static TvaClassificationSchemeType fromSchemeString(final String scheme) {
        for (final TvaClassificationSchemeType cs : values()) {
            if (cs.mScheme.equalsIgnoreCase(scheme)) {
                return cs;
            }
        }
        return TvaClassificationSchemeType.TVA_CS_UNKNOWN;
    }
}
