package eu.hradio.core.radiodns;

import org.minidns.record.*;
import org.omri.radioservice.*;
import java.util.concurrent.*;
import androidx.annotation.NonNull;
import java.io.*;
import java.net.*;

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
                            RadioDnsServiceWeb.this.mAIL = parser.parse(RadioDnsServiceWeb.this.getConnection(RadioDnsServiceWeb.this.mAilUrl).getInputStream());
                            RadioDnsServiceWeb.this.callCallbacks();
                        }
                        catch (IOException ex) {}
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
    
    private HttpURLConnection getConnection(final String connUrl) throws IOException {
        final URL url = new URL(connUrl);
        final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        final int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == 301 || httpResponseCode == 302) {
            final String redirectUrl = conn.getHeaderField("Location");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                conn.disconnect();
                return this.getConnection(redirectUrl);
            }
        }
        return conn;
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
