package eu.hradio.core.radiodns;

import android.content.Context;

import androidx.annotation.NonNull;

import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.record.CNAME;
import org.minidns.record.SRV;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RadioDnsCore
{
    private static final String TAG = "RadioDnsCore";
    private final RadioService mLookupSrv;
    private final String mFqdn;
    private final String mRdnsSrvId;
    private final String mBearerUri;
    private volatile boolean mLookupRunning;
    private ArrayList<RadioDnsService> mFoundServices;
    private ExecutorService mCbExe;
    private ConcurrentLinkedQueue<RadioDnsCoreLookupCallback> mCallbacks;

    RadioDnsCore(@NonNull final RadioService lookupSrv) {
        this.mLookupRunning = false;
        this.mFoundServices = new ArrayList<RadioDnsService>();
        this.mCallbacks = new ConcurrentLinkedQueue<RadioDnsCoreLookupCallback>();
        this.mLookupSrv = lookupSrv;
        this.mCbExe = Executors.newFixedThreadPool(10);
        switch (this.mLookupSrv.getRadioServiceType()) {
            case RADIOSERVICE_TYPE_DAB:
            case RADIOSERVICE_TYPE_EDI: {
                final RadioServiceDab dabSrv = (RadioServiceDab)this.mLookupSrv;
                this.mFqdn = "0." + Integer.toHexString(dabSrv.getServiceId()) + "." + Integer.toHexString(dabSrv.getEnsembleId()) + "." + Integer.toHexString(dabSrv.getServiceId()).charAt(0) + Integer.toHexString(dabSrv.getEnsembleEcc()) + ".dab.radiodns.org";
                this.mRdnsSrvId = "dab/" + Integer.toHexString(dabSrv.getServiceId()).charAt(0) + Integer.toHexString(dabSrv.getEnsembleEcc()) + "/" + Integer.toHexString(dabSrv.getEnsembleId()) + "/" + Integer.toHexString(dabSrv.getServiceId()) + "/0";
                this.mBearerUri = "dab:" + Integer.toHexString(dabSrv.getServiceId()).charAt(0) + Integer.toHexString(dabSrv.getEnsembleEcc()) + "." + Integer.toHexString(dabSrv.getEnsembleId()) + "." + Integer.toHexString(dabSrv.getServiceId()) + ".0";
                break;
            }
            default: {
                this.mFqdn = null;
                this.mRdnsSrvId = null;
                this.mBearerUri = null;
                break;
            }
        }
    }

    public void coreLookup(@NonNull final RadioDnsCoreLookupCallback callback, @NonNull final Context context) {
        this.mCallbacks.offer(callback);
        if (this.mFoundServices.isEmpty() && this.mFqdn != null && this.mRdnsSrvId != null && this.mBearerUri != null) {
            if (!this.mLookupRunning) {
                this.mLookupRunning = true;
                final Thread lookupThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            AndroidUsingLinkProperties.setup(context);
                            final ResolverResult<CNAME> result = (ResolverResult<CNAME>) ResolverApi.INSTANCE.resolve(RadioDnsCore.this.mFqdn, (Class)CNAME.class);
                            if (result.wasSuccessful()) {
                                for (final CNAME cname : result.getAnswers()) {
                                    RadioDnsCore.this.resolveServiceRecords(cname, context);
                                }
                            }
                        }
                        catch (IOException ioExc) {
                            ioExc.printStackTrace();
                        }
                        finally {
                            RadioDnsCore.this.callCallbacks();
                            RadioDnsCore.this.mLookupRunning = false;
                        }
                    }
                };
                lookupThread.start();
            }
        }
        else {
            this.callCallbacks();
        }
    }

    private void callCallbacks() {
        if (this.mCallbacks.size() > 0) {
            final Object[] array;
            final Object[] cbs = array = this.mCallbacks.toArray();
            for (final Object cb : array) {
                this.mCbExe.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((RadioDnsCoreLookupCallback)cb).coreLookupFinished(RadioDnsCore.this.mLookupSrv, RadioDnsCore.this.mFoundServices);
                    }
                });
            }
            this.mCallbacks.clear();
        }
    }

    private void resolveServiceRecords(final CNAME cname, @NonNull final Context context) {
        for (final RadioDnsServiceType rdnsType : RadioDnsServiceType.values()) {
            try {
                AndroidUsingLinkProperties.setup(context);
                final String appFqdn = String.format("_%s._%s.%s", rdnsType.getAppName(), "tcp", cname.getTarget().toString());
                final ResolverResult<SRV> result = (ResolverResult<SRV>)ResolverApi.INSTANCE.resolve(appFqdn, (Class)SRV.class);
                if (result.wasSuccessful()) {
                    for (final SRV serviceRecord : result.getAnswers()) {
                        switch (rdnsType) {
                            case RADIO_VIS: {
                                this.mFoundServices.add(new RadioDnsServiceVis(serviceRecord, this.mRdnsSrvId, this.mBearerUri, rdnsType, this.mLookupSrv));
                                continue;
                            }
                            case RADIO_EPG: {
                                this.mFoundServices.add(new RadioDnsServiceEpg(serviceRecord, this.mRdnsSrvId, this.mBearerUri, rdnsType, this.mLookupSrv));
                                continue;
                            }
                            case RADIO_TAG: {
                                this.mFoundServices.add(new RadioDnsServiceTag(serviceRecord, this.mRdnsSrvId, this.mBearerUri, rdnsType, this.mLookupSrv));
                                continue;
                            }
                            case RADIO_WEB: {
                                this.mFoundServices.add(new RadioDnsServiceWeb(serviceRecord, this.mRdnsSrvId, this.mBearerUri, rdnsType, this.mLookupSrv));
                                continue;
                            }
                        }
                    }
                }
            }
            catch (IOException ioExc) {
                ioExc.printStackTrace();
            }
        }
    }
}
