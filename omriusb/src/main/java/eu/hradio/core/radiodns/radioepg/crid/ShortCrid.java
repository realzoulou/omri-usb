package eu.hradio.core.radiodns.radioepg.crid;

import java.io.*;

public class ShortCrid implements Serializable
{
    private static final long serialVersionUID = 955556841830610461L;
    private final int mShortId;
    
    public ShortCrid(final int shortId) {
        this.mShortId = shortId;
    }
    
    public int getShortId() {
        return this.mShortId;
    }
}
