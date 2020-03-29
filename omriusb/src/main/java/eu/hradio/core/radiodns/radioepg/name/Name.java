package eu.hradio.core.radiodns.radioepg.name;

import java.io.*;
import androidx.annotation.NonNull;

public class Name implements Serializable
{
    private static final long serialVersionUID = -7811004977698611113L;
    private final NameType mNameType;
    private final String mLang;
    private final String mName;
    
    public Name(@NonNull final NameType type, @NonNull final String lang, @NonNull String name) {
        this.mNameType = type;
        this.mLang = lang;
        if (name.length() > this.mNameType.getMaxCharacters()) {
            name = name.substring(0, this.mNameType.getMaxCharacters());
        }
        this.mName = name;
    }
    
    public Name(@NonNull final NameType type, @NonNull final String name) {
        this(type, "en", name);
    }
    
    public NameType getType() {
        return this.mNameType;
    }
    
    public String getLanguage() {
        return this.mLang;
    }
    
    public String getName() {
        return this.mName;
    }
}
