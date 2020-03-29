package eu.hradio.core.radiodns.radioepg.radiodns;

import java.io.*;
import androidx.annotation.NonNull;

public class RadioDns implements Serializable
{
    private static final long serialVersionUID = -8106508207939090256L;
    private final String mFqdn;
    private final String mSid;
    
    public RadioDns(@NonNull final String fqdn, @NonNull final String serviceIdentifier) {
        this.mFqdn = fqdn;
        this.mSid = serviceIdentifier;
    }
    
    public String getFqdn() {
        return this.mFqdn;
    }
    
    public String getServiceIdentifier() {
        return this.mSid;
    }
}
