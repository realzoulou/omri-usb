package eu.hradio.core.radiodns;

import androidx.annotation.NonNull;

import org.minidns.record.*;
import org.omri.radioservice.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class RadioDnsServiceEpg extends RadioDnsService
{
    private static final String TAG = "RadioDnsServiceEpg";
    private final String mSiUrl;
    private volatile boolean mSiParserRunning;
    private ExecutorService mCbExe;
    private ConcurrentLinkedQueue<RadioDnsServiceEpgSiCallback> mSiCallbacks;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<RadioDnsServiceEpgPiCallback>> mPiCallbacks;
    private ConcurrentHashMap<String, Boolean> mPiThreadsRunning;
    private RadioEpgServiceInformation mSi;
    private HashMap<String, RadioEpgProgrammeInformation> mPiMap;
    
    RadioDnsServiceEpg(final SRV srvRecord, final String rdnsSrvId, final String bearerUri, final RadioDnsServiceType srvType, final RadioService lookupSrv) {
        super(srvRecord, rdnsSrvId, bearerUri, srvType, lookupSrv);
        this.mSiParserRunning = false;
        this.mSiCallbacks = new ConcurrentLinkedQueue<RadioDnsServiceEpgSiCallback>();
        this.mPiCallbacks = new ConcurrentHashMap<String, CopyOnWriteArrayList<RadioDnsServiceEpgPiCallback>>();
        this.mPiThreadsRunning = new ConcurrentHashMap<String, Boolean>();
        this.mSi = null;
        this.mPiMap = new HashMap<String, RadioEpgProgrammeInformation>();
        this.mCbExe = Executors.newFixedThreadPool(10);
        this.mSiUrl = this.creatEpgSiUrl();
    }
    
    public void getServiceInformation(@NonNull final RadioDnsServiceEpgSiCallback cb) {
        this.mSiCallbacks.offer(cb);
        if (this.mSi == null) {
            if (!this.mSiParserRunning) {
                this.mSiParserRunning = true;
                final Thread piParserThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final RadioEpgSiParser parser = new RadioEpgSiParser();
                            RadioDnsServiceEpg.this.mSi = parser.parse(RadioDnsServiceEpg.this.getConnection(RadioDnsServiceEpg.this.mSiUrl).getInputStream());
                            RadioDnsServiceEpg.this.callSiCallbacks(RadioDnsServiceEpg.this);
                        }
                        catch (IOException ex) {}
                        finally {
                            RadioDnsServiceEpg.this.callSiCallbacks(RadioDnsServiceEpg.this);
                            RadioDnsServiceEpg.this.mSiParserRunning = false;
                        }
                    }
                });
                piParserThread.start();
            }
        }
        else {
            this.callSiCallbacks(this);
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
    
    public void getProgrammeInformation(@NonNull final RadioDnsServiceEpgPiCallback cb) {
        this.getProgrammeInformation(0, cb);
    }
    
    public void getProgrammeInformation(final int dayOffset, @NonNull final RadioDnsServiceEpgPiCallback cb) {
        final String dateString = this.createPiDate(dayOffset);
        final String piUrl = this.creatEpgPiUrl(dateString);
        if (!this.mPiCallbacks.containsKey(dateString)) {
            this.mPiCallbacks.put(dateString, new CopyOnWriteArrayList<RadioDnsServiceEpgPiCallback>() {
                {
                    this.add(cb);
                }
            });
        }
        else {
            final CopyOnWriteArrayList<RadioDnsServiceEpgPiCallback> cbs = this.mPiCallbacks.get(dateString);
            if (cbs != null) {
                cbs.add(cb);
            }
        }
        if (!this.mPiMap.containsKey(dateString)) {
            final Boolean running = this.mPiThreadsRunning.get(dateString);
            if (running == null || !running) {
                this.mPiThreadsRunning.put(dateString, true);
                final Thread piParserThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final RadioEpgPiParser parser = new RadioEpgPiParser();
                            RadioDnsServiceEpg.this.mPiMap.put(dateString, parser.parse(RadioDnsServiceEpg.this.getConnection(piUrl).getInputStream()));
                            RadioDnsServiceEpg.this.callPiCallbacks(dateString, RadioDnsServiceEpg.this);
                        }
                        catch (IOException ex) {}
                        finally {
                            RadioDnsServiceEpg.this.callPiCallbacks(dateString, RadioDnsServiceEpg.this);
                            RadioDnsServiceEpg.this.mPiThreadsRunning.put(dateString, false);
                        }
                    }
                });
                piParserThread.start();
            }
        }
        else {
            this.callPiCallbacks(dateString, this);
        }
    }
    
    private void callSiCallbacks(final RadioDnsServiceEpg rdnsService) {
        if (this.mSiCallbacks.size() > 0) {
            final Object[] array;
            final Object[] cbs = array = this.mSiCallbacks.toArray();
            for (final Object cb : array) {
                if (cb != null) {
                    this.mCbExe.execute(new Runnable() {
                        @Override
                        public void run() {
                            ((RadioDnsServiceEpgSiCallback)cb).serviceInformationRetrieved(RadioDnsServiceEpg.this.mSi, rdnsService);
                        }
                    });
                }
            }
            this.mSiCallbacks.clear();
        }
    }
    
    private void callPiCallbacks(final String date, final RadioDnsServiceEpg rdnsService) {
        if (this.mPiCallbacks.containsKey(date)) {
            synchronized (this) {
                final CopyOnWriteArrayList<RadioDnsServiceEpgPiCallback> cbs = this.mPiCallbacks.get(date);
                if (cbs != null) {
                    for (final RadioDnsServiceEpgPiCallback cb : cbs) {
                        if (cb != null) {
                            this.mCbExe.execute(new Runnable() {
                                @Override
                                public void run() {
                                    cb.programmeInformationRetrieved(RadioDnsServiceEpg.this.mPiMap.get(date), rdnsService);
                                }
                            });
                        }
                    }
                }
                this.mPiCallbacks.remove(date);
            }
        }
    }
    
    private String creatEpgSiUrl() {
        final StringBuilder silUrlBuilder = new StringBuilder();
        silUrlBuilder.append("http://").append(this.getTarget());
        if (this.getPort() != 80) {
            silUrlBuilder.append(":").append(Integer.toString(this.getPort()));
        }
        silUrlBuilder.append("/radiodns/spi/3.1/SI.xml");
        return silUrlBuilder.toString();
    }
    
    private String creatEpgPiUrl(final String dateString) {
        final StringBuilder pilUrlBuilder = new StringBuilder();
        pilUrlBuilder.append("http://").append(this.getTarget());
        if (this.getPort() != 80) {
            pilUrlBuilder.append(":").append(Integer.toString(this.getPort()));
        }
        pilUrlBuilder.append("/radiodns/spi/3.1/").append(this.getServiceIdentifier()).append("/");
        pilUrlBuilder.append(dateString);
        pilUrlBuilder.append("_PI.xml");
        return pilUrlBuilder.toString();
    }
    
    private String createPiDate(final int dayOffset) {
        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(1);
        final int month = cal.get(2);
        final int day = cal.get(5);
        cal.set(year, month, day + dayOffset);
        final DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        final Date dateOffset = new Date(cal.getTimeInMillis());
        return df.format(dateOffset);
    }
}
