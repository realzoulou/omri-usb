package eu.hradio.core.radiodns.radioepg.crid;

import java.io.*;
import androidx.annotation.NonNull;

public class Crid implements Serializable
{
    private static final long serialVersionUID = 3142336311505710334L;
    private final String mCridString;
    private final String mCridAuthority;
    private final String mCridData;
    
    public Crid(@NonNull final String cridId) {
        this.mCridString = cridId.trim();
        final String[] split = this.mCridString.split("/");
        if (split.length >= 3) {
            this.mCridAuthority = split[2];
            final StringBuilder crdBuilder = new StringBuilder();
            for (int rem = 3; rem < split.length; ++rem) {
                crdBuilder.append(split[rem]);
                crdBuilder.append("/");
            }
            crdBuilder.deleteCharAt(crdBuilder.length() - 1);
            this.mCridData = crdBuilder.toString();
            return;
        }
        this.mCridAuthority = "";
        this.mCridData = "";
    }
    
    public String getCridString() {
        return this.mCridString;
    }
    
    public String getCridAuthority() {
        return this.mCridAuthority;
    }
    
    public String getCridData() {
        return this.mCridData;
    }
}
