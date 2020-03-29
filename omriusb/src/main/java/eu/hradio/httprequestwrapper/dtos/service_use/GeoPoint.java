package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class GeoPoint implements Serializable
{
    private static final long serialVersionUID = -6969361869871861170L;
    private double lat;
    private double lon;
    
    public Double getLon() {
        return this.lon;
    }
    
    public void setLon(final Double longitude) {
        this.lon = longitude;
    }
    
    public Double getLat() {
        return this.lat;
    }
    
    public void setLat(final Double latitude) {
        this.lat = latitude;
    }
}
