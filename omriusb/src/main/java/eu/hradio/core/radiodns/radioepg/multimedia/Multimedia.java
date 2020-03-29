package eu.hradio.core.radiodns.radioepg.multimedia;

import java.io.*;
import androidx.annotation.NonNull;

public class Multimedia implements Serializable
{
    private static final long serialVersionUID = 1717605927046874600L;
    private final MultimediaType mType;
    private final String mLang;
    private final String mUrl;
    private final int mWidth;
    private final int mHeight;
    private final MultimediaTransferType mTransferType;
    private final String mMime;
    
    public Multimedia(@NonNull final MultimediaType type, @NonNull final String lang, @NonNull final String url, @NonNull final String mime, final int width, final int height) {
        this.mType = type;
        this.mLang = lang;
        this.mUrl = url;
        this.mWidth = width;
        this.mHeight = height;
        this.mMime = mime;
        if (url.startsWith(MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_MOT.getTransferSchema())) {
            this.mTransferType = MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_MOT;
        }
        else if (url.startsWith(MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_HTTP.getTransferSchema())) {
            this.mTransferType = MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_HTTP;
        }
        else if (url.startsWith(MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_HTTPS.getTransferSchema())) {
            this.mTransferType = MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_HTTPS;
        }
        else {
            this.mTransferType = MultimediaTransferType.MULTIMEDIA_TRANSFER_TYPE_UNKNOWN;
        }
    }
    
    public Multimedia(@NonNull final MultimediaType type, @NonNull final String lang, @NonNull final String url) {
        this(type, lang, url, type.getMime(), type.getWidth(), type.getHeight());
    }
    
    public Multimedia(@NonNull final MultimediaType type, @NonNull final String url) {
        this(type, "en", url, type.getMime(), type.getWidth(), type.getHeight());
    }
    
    public MultimediaType getType() {
        return this.mType;
    }
    
    public String getLanguage() {
        return this.mLang;
    }
    
    public String getUrl() {
        return this.mUrl;
    }
    
    public int getWidth() {
        return this.mWidth;
    }
    
    public int getHeight() {
        return this.mHeight;
    }
    
    public String getMime() {
        return this.mMime;
    }
    
    public MultimediaTransferType getTranferType() {
        return this.mTransferType;
    }
}
