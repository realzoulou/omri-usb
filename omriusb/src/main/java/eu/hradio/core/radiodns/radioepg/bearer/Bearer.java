package eu.hradio.core.radiodns.radioepg.bearer;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.geolocation.*;
import androidx.annotation.NonNull;
import java.util.*;

public class Bearer implements Serializable
{
    private static final long serialVersionUID = -4369723785425543511L;
    private final String mBearerIdString;
    private final int mCost;
    private final String mMimeString;
    private final int mBitrate;
    private final int mOffset;
    private final BearerType mBearerType;
    private List<GeoLocation> mGeoLocations;
    
    public Bearer(@NonNull final String bearerId, final int cost, final String mime, final int bitrate, final int offset) {
        this.mGeoLocations = new ArrayList<GeoLocation>();
        this.mBearerIdString = bearerId;
        this.mCost = cost;
        this.mMimeString = mime;
        this.mBitrate = bitrate;
        this.mOffset = offset;
        final String[] bearerTypeId = bearerId.split(":");
        if (bearerTypeId.length > 0) {
            for (final BearerType type : BearerType.values()) {
                if (type.getBearerTypeId().equalsIgnoreCase(bearerTypeId[0])) {
                    this.mBearerType = type;
                    return;
                }
            }
        }
        this.mBearerType = BearerType.BEARER_TYPE_UNKNOWN;
    }
    
    public Bearer(@NonNull final String bearerId, final int cost, final String mime, final int bitrate) {
        this(bearerId, cost, mime, bitrate, 0);
    }
    
    public void addGeoLocations(final List<GeoLocation> geoLocations) {
        this.mGeoLocations.addAll(geoLocations);
    }
    
    public void addGeoLocation(final GeoLocation geoLocation) {
        this.mGeoLocations.add(geoLocation);
    }
    
    public String getBearerIdString() {
        return this.mBearerIdString;
    }
    
    public int getCost() {
        return this.mCost;
    }
    
    public String getMimeType() {
        return this.mMimeString;
    }
    
    public int getBitrate() {
        return this.mBitrate;
    }
    
    public int getOffset() {
        return this.mOffset;
    }
    
    public BearerType getBearerType() {
        return this.mBearerType;
    }
    
    public List<GeoLocation> getGeoLocations() {
        return this.mGeoLocations;
    }
}
