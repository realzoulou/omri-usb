package eu.hradio.core.radiodns.radioepg.geolocation;

import java.io.*;
import java.util.*;

public class GeoLocationPolygon implements Serializable
{
    private static final long serialVersionUID = -1825858425186748395L;
    private final List<GeoLocationPoint> mPolygonEntries;
    
    public GeoLocationPolygon(final List<GeoLocationPoint> polygonEntries) {
        this.mPolygonEntries = polygonEntries;
    }
}
