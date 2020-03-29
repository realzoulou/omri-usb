package eu.hradio.core.radiodns.radioepg.keywords;

import java.io.*;
import androidx.annotation.NonNull;

public class Keywords implements Serializable
{
    private static final long serialVersionUID = 582624195273709567L;
    private final String[] mKeywords;
    private final String mLang;
    
    public Keywords(@NonNull final String keywords, @NonNull final String lang) {
        this.mKeywords = keywords.split(",");
        this.mLang = lang;
    }
    
    public Keywords(@NonNull final String keywords) {
        this(keywords, "en");
    }
    
    public final String[] getKeywords() {
        return this.mKeywords;
    }
}
