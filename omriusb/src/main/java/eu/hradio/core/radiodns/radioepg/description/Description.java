package eu.hradio.core.radiodns.radioepg.description;

import java.io.*;
import androidx.annotation.NonNull;

public class Description implements Serializable
{
    private static final long serialVersionUID = -1774662507842244772L;
    private final DescriptionType mType;
    private final String mLang;
    private final String mDesc;
    
    public Description(@NonNull final DescriptionType type, @NonNull final String lang, @NonNull String description) {
        this.mType = type;
        this.mLang = lang;
        if (description.length() > this.mType.getMaxCharacters()) {
            description = description.substring(0, this.mType.getMaxCharacters());
        }
        this.mDesc = description;
    }
    
    public Description(@NonNull final DescriptionType type, @NonNull final String description) {
        this(type, "en", description);
    }
    
    public DescriptionType getType() {
        return this.mType;
    }
    
    public String getLanguage() {
        return this.mLang;
    }
    
    public String getDescription() {
        return this.mDesc;
    }
}
