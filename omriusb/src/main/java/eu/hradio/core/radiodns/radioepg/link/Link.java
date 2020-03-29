package eu.hradio.core.radiodns.radioepg.link;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.time.*;
import androidx.annotation.NonNull;

public class Link implements Serializable
{
    private static final long serialVersionUID = -4143887406286480645L;
    private final String mUri;
    private final String mMime;
    private final String mLang;
    private final String mDescription;
    private final TimePoint mExpiryTime;
    
    public Link(@NonNull final String uri, @NonNull final String mime, @NonNull final String language, @NonNull String description, @NonNull final String expiryTime) {
        this.mUri = uri;
        this.mMime = mime;
        this.mLang = language;
        if (description.length() > 180) {
            description = description.substring(0, 179);
        }
        this.mDescription = description;
        this.mExpiryTime = new TimePoint(expiryTime);
    }
    
    public Link(@NonNull final String uri, @NonNull final String mime, @NonNull final String language, @NonNull final String description) {
        this(uri, mime, language, description, "");
    }
    
    public Link(@NonNull final String uri, @NonNull final String mime, @NonNull final String language) {
        this(uri, mime, language, "");
    }
    
    public Link(@NonNull final String uri, @NonNull final String mime) {
        this(uri, mime, "en");
    }
    
    public Link(@NonNull final String uri) {
        this(uri, "");
    }
    
    public String getUri() {
        return this.mUri;
    }
    
    public String getMime() {
        return this.mMime;
    }
    
    public String getLanguage() {
        return this.mLang;
    }
    
    public String getDescription() {
        return this.mDescription;
    }
    
    public TimePoint getExpiryTime() {
        return this.mExpiryTime;
    }
}
