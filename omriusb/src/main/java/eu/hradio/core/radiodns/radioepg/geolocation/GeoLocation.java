package eu.hradio.core.radiodns.radioepg.geolocation;

import java.io.*;
import java.util.*;

public class GeoLocation implements Serializable
{
    private static final long serialVersionUID = -7788123122934098187L;
    private List<String> mCountryStrings;
    private List<GeoLocationPoint> mLocationPoints;
    private List<GeoLocationPolygon> mLocationPolygons;
    
    public GeoLocation() {
        this.mCountryStrings = new ArrayList<String>();
        this.mLocationPoints = new ArrayList<GeoLocationPoint>();
        this.mLocationPolygons = new ArrayList<GeoLocationPolygon>();
    }
    
    public void addCountryString(final String countryString) {
        this.mCountryStrings.add(countryString);
    }
    
    public void addLocationPolygon(final GeoLocationPolygon locationPolygon) {
        this.mLocationPolygons.add(locationPolygon);
    }
    
    public void addLocationPoint(final GeoLocationPoint locationPoint) {
        this.mLocationPoints.add(locationPoint);
    }
    
    public void addCountryStrings(final List<String> countryStrings) {
        this.mCountryStrings.addAll(countryStrings);
    }
    
    public void addLocationPolygons(final List<GeoLocationPolygon> locationPolygons) {
        this.mLocationPolygons.addAll(locationPolygons);
    }
    
    public void addLocationPoints(final List<GeoLocationPoint> locationPoints) {
        this.mLocationPoints.addAll(locationPoints);
    }
    
    public List<String> getCountryStrings() {
        return this.mCountryStrings;
    }
    
    public List<GeoLocationPoint> getLocationPoints() {
        return this.mLocationPoints;
    }
    
    public List<GeoLocationPolygon> getLocationPolygons() {
        return this.mLocationPolygons;
    }
}
