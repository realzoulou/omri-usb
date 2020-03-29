package eu.hradio.core.radiodns;

import org.minidns.record.*;
import org.omri.radioservice.*;

public class RadioDnsServiceTag extends RadioDnsService
{
    private static final String TAG = "RadioDnsServiceTag";
    
    RadioDnsServiceTag(final SRV srvRecord, final String rdnsSrvId, final String bearerUri, final RadioDnsServiceType srvType, final RadioService lookupSrv) {
        super(srvRecord, rdnsSrvId, bearerUri, srvType, lookupSrv);
    }
}
