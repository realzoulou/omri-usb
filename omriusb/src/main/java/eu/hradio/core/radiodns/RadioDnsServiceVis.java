package eu.hradio.core.radiodns;

import net.ser1.stomp.*;
import org.minidns.record.*;
import java.util.concurrent.*;
import org.omri.radioservice.*;
import androidx.annotation.NonNull;
import javax.security.auth.login.*;
import java.util.*;
import java.text.*;
import org.omri.radioservice.metadata.*;
import java.net.*;
import java.io.*;

public class RadioDnsServiceVis extends RadioDnsService
{
    private static final String TAG = "RadioDnsServiceVis";
    private ExecutorService mCbExe;
    private CopyOnWriteArrayList<VisualMetadataListener> mVisualSubscibers;
    private CopyOnWriteArrayList<TextualMetadataListener> mTextualSubscibers;
    private Client mStompClient;
    private final String mImageTopicString;
    private final String mTextTopicString;
    private Timer mConnectedTimer;
    private Listener mStompErrorListener;
    private Listener mStompImagesListener;
    private Listener mStompTextListener;
    private static final String RADIOVIS_TRIGGERTIME = "trigger-time";
    private static final String RADIOVIS_CATEGORY_ID = "CategoryId";
    private static final String RADIOVIS_SLIDE_ID = "SlideId";
    private static final String RADIOVIS_LINK = "link";
    private static final String RADIOVIS_CATEGORY_TITLE = "CategoryTitle";
    private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    
    RadioDnsServiceVis(final SRV srvRecord, final String rdnsSrvId, final String bearerUri, final RadioDnsServiceType srvType, final RadioService lookupSrv) {
        super(srvRecord, rdnsSrvId, bearerUri, srvType, lookupSrv);
        this.mVisualSubscibers = new CopyOnWriteArrayList<VisualMetadataListener>();
        this.mTextualSubscibers = new CopyOnWriteArrayList<TextualMetadataListener>();
        this.mConnectedTimer = null;
        this.mStompErrorListener = (Listener)new Listener() {
            public void message(final Map headers, final String body) {
            }
        };
        this.mStompImagesListener = (Listener)new Listener() {
            public void message(final Map headers, final String body) {
                RadioDnsServiceVis.this.mCbExe.execute(new VisDownload(headers, body));
            }
        };
        this.mStompTextListener = (Listener)new Listener() {
            public void message(final Map headers, final String body) {
                if (body.length() > 5) {
                    final RadioVisTextual textual = new RadioVisTextual(body.substring(5));
                    for (final TextualMetadataListener cb : RadioDnsServiceVis.this.mTextualSubscibers) {
                        RadioDnsServiceVis.this.mCbExe.execute(new Runnable() {
                            @Override
                            public void run() {
                                cb.newTextualMetadata((Textual)textual);
                            }
                        });
                    }
                }
            }
        };
        this.mCbExe = Executors.newFixedThreadPool(10);
        this.mImageTopicString = this.createImageTopic();
        this.mTextTopicString = this.createTextTopic();
    }
    
    public void subscribe(@NonNull final RadioServiceListener callback) {
        if (callback instanceof TextualMetadataListener) {
            this.mTextualSubscibers.add((TextualMetadataListener)callback);
        }
        if (callback instanceof VisualMetadataListener) {
            this.mVisualSubscibers.add((VisualMetadataListener)callback);
        }
        if (!this.mVisualSubscibers.isEmpty() && !this.mTextualSubscibers.isEmpty() && this.mStompClient == null) {
            this.connectStompClient();
            if (this.mConnectedTimer == null) {
                (this.mConnectedTimer = new Timer()).scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (!RadioDnsServiceVis.this.mStompClient.isConnected()) {
                            RadioDnsServiceVis.this.mStompClient = null;
                            RadioDnsServiceVis.this.connectStompClient();
                        }
                    }
                }, 0L, 1000L);
            }
        }
    }
    
    private void connectStompClient() {
        if (this.mStompClient == null) {
            try {
                (this.mStompClient = new Client(this.getTarget(), this.getPort(), "", "")).addErrorListener(this.mStompErrorListener);
                if (this.mStompClient.isConnected()) {}
                this.mStompClient.subscribe(this.mTextTopicString, this.mStompTextListener);
                this.mStompClient.subscribe(this.mImageTopicString, this.mStompImagesListener);
            }
            catch (LoginException ex) {}
            catch (IOException ex2) {}
        }
    }
    
    public void unsubscribe(@NonNull final RadioServiceListener callback) {
        if (callback instanceof TextualMetadataListener) {
            this.mTextualSubscibers.remove(callback);
        }
        if (callback instanceof VisualMetadataListener) {
            this.mVisualSubscibers.remove(callback);
        }
        if (this.mVisualSubscibers.isEmpty() && this.mTextualSubscibers.isEmpty() && this.mStompClient != null) {
            if (this.mConnectedTimer != null) {
                this.mConnectedTimer.cancel();
                this.mConnectedTimer = null;
            }
            if (this.mStompClient.isConnected()) {
                this.mStompClient.unsubscribe(this.mImageTopicString);
                this.mStompClient.unsubscribe(this.mTextTopicString);
                this.mStompClient.disconnect();
                this.mStompClient = null;
            }
        }
    }
    
    private String createImageTopic() {
        return "/topic/" + this.getServiceIdentifier() + "/image";
    }
    
    private String createTextTopic() {
        return "/topic/" + this.getServiceIdentifier() + "/text";
    }
    
    private class RadioVisTextual implements Textual
    {
        private final String mText;
        
        RadioVisTextual(final String text) {
            this.mText = text;
        }
        
        public TextualType getType() {
            return TextualType.METADATA_TEXTUAL_TYPE_RADIODNS_RADIOVIS;
        }
        
        public String getText() {
            return this.mText;
        }
    }
    
    private class RadioVisVisual implements VisualIpRdnsRadioVis
    {
        private Calendar mTriggerCal;
        private int mCatId;
        private int mSlideId;
        private String mCatTitle;
        private String mLink;
        private VisualMimeType mVisType;
        private int mVisWidth;
        private int mVisHeight;
        private final byte[] mVisData;
        
        RadioVisVisual(final Map headers, final byte[] visData, final VisualMimeType mime) {
            this.mTriggerCal = null;
            this.mCatId = -1;
            this.mSlideId = 0;
            this.mCatTitle = "";
            this.mLink = "";
            this.mVisType = VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN;
            this.mVisWidth = -1;
            this.mVisHeight = -1;
            final String triggerTime = (String) headers.get("trigger-time");
            final SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault());
            if (triggerTime != null && !triggerTime.equals("NOW")) {
                try {
                    iso8601format.parse(triggerTime.replaceAll("Z$", "+00:00"));
                    this.mTriggerCal = iso8601format.getCalendar();
                }
                catch (ParseException ex) {}
            }
            final String catIdString = (String) headers.get("CategoryId");
            if (catIdString != null) {
                try {
                    this.mCatId = Integer.parseInt(catIdString);
                }
                catch (NumberFormatException ex2) {}
            }
            final String slideIdString = (String) headers.get("SlideId");
            if (slideIdString != null) {
                try {
                    this.mSlideId = Integer.parseInt(slideIdString);
                }
                catch (NumberFormatException ex3) {}
            }
            final String linkString = (String) headers.get("link");
            if (linkString != null) {
                this.mLink = linkString;
            }
            final String catTitleString = (String) headers.get("CategoryTitle");
            if (catTitleString != null) {
                this.mCatTitle = catTitleString;
            }
            this.mVisData = visData;
        }
        
        public Calendar getTriggerTime() {
            return this.mTriggerCal;
        }
        
        public VisualType getVisualType() {
            return VisualType.METADATA_VISUAL_TYPE_RADIODNS_RADIOVIS;
        }
        
        public VisualMimeType getVisualMimeType() {
            return this.mVisType;
        }
        
        public byte[] getVisualData() {
            return this.mVisData;
        }
        
        public int getVisualWidth() {
            return this.mVisWidth;
        }
        
        public int getVisualHeight() {
            return this.mVisHeight;
        }
        
        public String getLink() {
            return this.mLink;
        }
        
        public int getCategoryId() {
            return this.mCatId;
        }
        
        public int getSlideId() {
            return this.mSlideId;
        }
        
        public String getCategoryTitle() {
            return this.mCatTitle;
        }
    }
    
    private class VisDownload implements Runnable
    {
        private final Map mHeaders;
        private final String mBody;
        
        private VisDownload(final Map headers, final String body) {
            this.mHeaders = headers;
            if (body.startsWith("SHOW ")) {
                this.mBody = body.substring(5);
            }
            else {
                this.mBody = body;
            }
        }
        
        @Override
        public void run() {
            try {
                final URL visUrl = new URL(this.mBody);
                final HttpURLConnection visConn = (HttpURLConnection)visUrl.openConnection();
                visConn.setInstanceFollowRedirects(true);
                visConn.setConnectTimeout(5000);
                visConn.setReadTimeout(5000);
                if (visConn.getResponseCode() == 200) {
                    final String contentLengthString = visConn.getHeaderField("Content-Length");
                    final String contentTypeString = visConn.getHeaderField("Content-Type");
                    int contentLength = 0;
                    if (contentLengthString != null) {
                        contentLength = Integer.parseInt(contentLengthString.trim());
                    }
                    VisualMimeType visMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN;
                    if (contentTypeString != null) {
                        final String trim = contentTypeString.trim();
                        switch (trim) {
                            case "image/jpeg":
                            case "image/jpg": {
                                visMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_JPEG;
                                break;
                            }
                            case "image/bmp": {
                                visMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_BMP;
                                break;
                            }
                            case "image/gif": {
                                visMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_GIF;
                                break;
                            }
                            case "image/png": {
                                visMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_PNG;
                                break;
                            }
                        }
                    }
                    final InputStream visInputStream = visConn.getInputStream();
                    final ByteArrayOutputStream visOutputStream = new ByteArrayOutputStream();
                    final byte[] inBuff = new byte[1024];
                    int bytesred = 0;
                    int readLength;
                    while ((readLength = visInputStream.read(inBuff)) > 0) {
                        visOutputStream.write(inBuff, 0, readLength);
                        bytesred += readLength;
                    }
                    if (bytesred > 0 && bytesred == contentLength) {
                        final RadioVisVisual vis = new RadioVisVisual(this.mHeaders, visOutputStream.toByteArray(), visMime);
                        for (final VisualMetadataListener cb : RadioDnsServiceVis.this.mVisualSubscibers) {
                            RadioDnsServiceVis.this.mCbExe.execute(new Runnable() {
                                @Override
                                public void run() {
                                    cb.newVisualMetadata((Visual)vis);
                                }
                            });
                        }
                    }
                }
            }
            catch (MalformedURLException ex) {}
            catch (SocketTimeoutException ex2) {}
            catch (IOException ex3) {}
        }
    }
}
