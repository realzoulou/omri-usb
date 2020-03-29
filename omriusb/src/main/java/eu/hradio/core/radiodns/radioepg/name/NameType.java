package eu.hradio.core.radiodns.radioepg.name;

public enum NameType
{
    NAME_SHORT(8), 
    NAME_MEDIUM(16), 
    NAME_LONG(128);
    
    private final int mMaxChars;
    
    private NameType(final int maxChars) {
        this.mMaxChars = maxChars;
    }
    
    public int getMaxCharacters() {
        return this.mMaxChars;
    }
}
