package eu.hradio.core.radiodns;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.mediadescription.*;
import eu.hradio.core.radiodns.radioepg.scope.*;
import java.util.*;

public class RadioWebApplication implements Serializable
{
    private static final long serialVersionUID = -8142697515025657146L;
    private static final String TAG = "RadioWebApplication";
    private final RadioWebApplicationControl mControlCode;
    private final int mAppId;
    private final int mAppPrio;
    private List<Bearer> mBearers;
    private List<Name> mAppNames;
    private List<MediaDescription> mDescs;
    private List<ServiceScope> mScopes;
    
    RadioWebApplication(final RadioWebApplicationControl controlCode, final int appId, final int appPrio) {
        this.mBearers = new ArrayList<Bearer>();
        this.mAppNames = new ArrayList<Name>();
        this.mDescs = new ArrayList<MediaDescription>();
        this.mScopes = new ArrayList<ServiceScope>();
        this.mControlCode = controlCode;
        this.mAppId = appId;
        this.mAppPrio = appPrio;
    }
    
    void addBearer(final Bearer bearer) {
        this.mBearers.add(bearer);
    }
    
    void addName(final Name name) {
        this.mAppNames.add(name);
    }
    
    void addMediaDescription(final MediaDescription md) {
        this.mDescs.add(md);
    }
    
    void addServiceScope(final ServiceScope scope) {
        this.mScopes.add(scope);
    }
    
    public RadioWebApplicationControl getControlCode() {
        return this.mControlCode;
    }
    
    public int getApplicationId() {
        return this.mAppId;
    }
    
    public int getApplicationPriority() {
        return this.mAppPrio;
    }
    
    public List<ServiceScope> getScopes() {
        return this.mScopes;
    }
    
    public List<Bearer> getBearers() {
        return this.mBearers;
    }
    
    public List<Name> getNames() {
        return this.mAppNames;
    }
    
    public List<MediaDescription> getMediaDescriptions() {
        return this.mDescs;
    }
}
