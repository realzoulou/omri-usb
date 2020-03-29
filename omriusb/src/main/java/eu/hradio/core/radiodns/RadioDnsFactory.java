package eu.hradio.core.radiodns;

import androidx.annotation.NonNull;

import java.util.concurrent.*;
import org.omri.radioservice.*;

public class RadioDnsFactory
{
    private static final String TAG = "RadioDnsFactory";
    private static ConcurrentHashMap<RadioService, RadioDnsCore> mLookupCache;
    
    public static RadioDnsCore createCoreLookup(@NonNull final RadioService lookupSrv, final boolean noCache) {
        if (noCache) {
            return new RadioDnsCore(lookupSrv);
        }
        if (RadioDnsFactory.mLookupCache.containsKey(lookupSrv)) {
            return RadioDnsFactory.mLookupCache.get(lookupSrv);
        }
        final RadioDnsCore rdns = new RadioDnsCore(lookupSrv);
        RadioDnsFactory.mLookupCache.put(lookupSrv, rdns);
        return rdns;
    }
    
    public static void clearCache() {
        RadioDnsFactory.mLookupCache.clear();
    }
    
    static {
        RadioDnsFactory.mLookupCache = new ConcurrentHashMap<RadioService, RadioDnsCore>();
    }
}
