package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class Location implements Serializable
{
    private static final long serialVersionUID = -7788562614944501565L;
    private String timezone;
    private GeoPoint geoPoint;
    private String countryCode;
    private int distance;
    
    public String getTimezone() {
        return this.timezone;
    }
    
    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }
    
    public GeoPoint getGeoPoint() {
        return this.geoPoint;
    }
    
    public void setGeoPoint(final GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
    
    public String getCountryCode() {
        return this.countryCode;
    }
    
    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }
    
    public int getDistance() {
        return this.distance;
    }
    
    public void setDistance(final int distance) {
        this.distance = distance;
    }
}
