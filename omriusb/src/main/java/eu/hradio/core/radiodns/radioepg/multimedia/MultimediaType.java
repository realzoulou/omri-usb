package eu.hradio.core.radiodns.radioepg.multimedia;

public enum MultimediaType
{
    MULTIMEDIA_LOGO_UNRESTRICTED(-1, -1, ""), 
    MULTIMEDIA_LOGO_SQUARE(32, 32, "image/png"), 
    MULTIMEDIA_LOGO_RECTANGLE(32, 112, "image/png");
    
    private final int mWidth;
    private final int mHeight;
    private final String mMime;
    
    private MultimediaType(final int width, final int height, final String mime) {
        this.mWidth = width;
        this.mHeight = height;
        this.mMime = mime;
    }
    
    public int getWidth() {
        return this.mWidth;
    }
    
    public int getHeight() {
        return this.mHeight;
    }
    
    public String getMime() {
        return this.mMime;
    }
    
    public static MultimediaType fromTypeString(final String typeString) {
        if (typeString != null) {
            if (typeString.equalsIgnoreCase("logo_colour_square")) {
                return MultimediaType.MULTIMEDIA_LOGO_SQUARE;
            }
            if (typeString.equalsIgnoreCase("logo_colour_rectangle")) {
                return MultimediaType.MULTIMEDIA_LOGO_RECTANGLE;
            }
        }
        return MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED;
    }
}
