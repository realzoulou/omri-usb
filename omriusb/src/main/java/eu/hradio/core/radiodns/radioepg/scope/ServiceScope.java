package eu.hradio.core.radiodns.radioepg.scope;

import java.io.*;
import androidx.annotation.NonNull;

public class ServiceScope implements Serializable
{
    private static final long serialVersionUID = 1332589507616948018L;
    private final String mServiceScope;
    
    public ServiceScope(@NonNull final String serviceScopeString) {
        this.mServiceScope = serviceScopeString;
    }
}
