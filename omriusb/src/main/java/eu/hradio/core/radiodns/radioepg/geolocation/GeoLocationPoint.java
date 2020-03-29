package eu.hradio.core.radiodns.radioepg.geolocation;

import java.io.*;

public class GeoLocationPoint implements Serializable
{
    private static final long serialVersionUID = 7803586796620543333L;
    private final double mLatitude;
    private final double mLongitude;
    
    public GeoLocationPoint(final double latitude, final double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }
    
    public double getLatitude() {
        return this.mLatitude;
    }
    
    public double getLongitude() {
        return this.mLongitude;
    }
}
