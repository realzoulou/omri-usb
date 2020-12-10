package eu.hradio.core.radiodns;

import android.util.Log;

import androidx.annotation.NonNull;

import org.minidns.record.SRV;
import org.omri.BuildConfig;
import org.omri.radio.impl.IpServiceScanner;
import org.omri.radioservice.RadioService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RadioDnsServiceWeb extends RadioDnsService
{
    private static final String TAG = "RadioDnsServiceWeb";
    private final String mAilUrl;
    private RadioWebApplicationInformationList mAIL;
    private volatile boolean mAppParserRunning;
    private ExecutorService mCbExe;
    private ConcurrentLinkedQueue<RadioDnsServiceWebCallback> mCallbacks;
    
    RadioDnsServiceWeb(final SRV srvRecord, final String rdnsSrvId, final String bearerUri, final RadioDnsServiceType srvType, final RadioService lookupSrv) {
        super(srvRecord, rdnsSrvId, bearerUri, srvType, lookupSrv);
        this.mAIL = null;
        this.mAppParserRunning = false;
        this.mCallbacks = new ConcurrentLinkedQueue<RadioDnsServiceWebCallback>();
        this.mCbExe = Executors.newFixedThreadPool(10);
        this.mAilUrl = this.createRwebUrl();
    }
    
    public void getApplicationInformationList(@NonNull final RadioDnsServiceWebCallback callback) {
        this.mCallbacks.offer(callback);
        if (this.mAIL == null) {
            if (!this.mAppParserRunning) {
                this.mAppParserRunning = true;
                final Thread appParserThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final RadioWebApplicationParser parser = new RadioWebApplicationParser();
                            final HttpURLConnection urlConnection = IpServiceScanner.getConnection(mAilUrl);
                            if (urlConnection != null) {
                                RadioDnsServiceWeb.this.mAIL = parser.parse(urlConnection.getInputStream());
                                if (BuildConfig.DEBUG) Log.i(TAG, "getApplicationInformationList: parsed " + urlConnection.getURL().toString());
                            } else {
                                Log.w(TAG,"failed to connect to " + mAilUrl);
                            }
                            RadioDnsServiceWeb.this.callCallbacks();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            RadioDnsServiceWeb.this.callCallbacks();
                            RadioDnsServiceWeb.this.mAppParserRunning = false;
                        }
                    }
                });
                appParserThread.start();
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
                        ((RadioDnsServiceWebCallback)cb).applicationInformationListRetrieved(RadioDnsServiceWeb.this.mAIL, RadioDnsServiceWeb.this.getRadioService());
                    }
                });
            }
            this.mCallbacks.clear();
        }
    }
    
    private String createRwebUrl() {
        final StringBuilder ailUrlBuilder = new StringBuilder();
        ailUrlBuilder.append("http://").append(this.getTarget());
        if (this.getPort() != 80) {
            ailUrlBuilder.append(":").append(Integer.toString(this.getPort()));
        }
        ailUrlBuilder.append("/radiodns/web/");
        ailUrlBuilder.append(this.getServiceIdentifier());
        ailUrlBuilder.append("/AIL.xml");
        return ailUrlBuilder.toString();
    }
}
