package eu.hradio.core.radiodns;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import eu.hradio.core.radiodns.radioepg.bearer.Bearer;
import eu.hradio.core.radiodns.radioepg.description.Description;
import eu.hradio.core.radiodns.radioepg.description.DescriptionType;
import eu.hradio.core.radiodns.radioepg.genre.Genre;
import eu.hradio.core.radiodns.radioepg.geolocation.GeoLocation;
import eu.hradio.core.radiodns.radioepg.geolocation.GeoLocationPoint;
import eu.hradio.core.radiodns.radioepg.geolocation.GeoLocationPolygon;
import eu.hradio.core.radiodns.radioepg.link.Link;
import eu.hradio.core.radiodns.radioepg.mediadescription.MediaDescription;
import eu.hradio.core.radiodns.radioepg.multimedia.Multimedia;
import eu.hradio.core.radiodns.radioepg.multimedia.MultimediaType;
import eu.hradio.core.radiodns.radioepg.name.Name;
import eu.hradio.core.radiodns.radioepg.name.NameType;
import eu.hradio.core.radiodns.radioepg.scope.ServiceScope;

import static org.omri.BuildConfig.DEBUG;

abstract class RadioEpgParser
{
    static final String TAG = "REpgParser";
    static final String NAMESPACE;
    static final String SERVICEINFORMATION_TAG = "serviceInformation";
    static final String CREATIONTIME_ATTR = "creationTime";
    static final String ORIGINATOR_ATTR = "originator";
    static final String XMLLANG_ATTR = "xml:lang";
    static final String VERSION_ATTR = "version";
    static final String TERMS_ATTR = "terms";
    static final String SERVICES_TAG = "services";
    static final String SERVICEPROVIDER_TAG = "serviceProvider";
    static final String SERVICE_TAG = "service";
    static final String DESCRIPTION_SHORT_TAG = "shortDescription";
    static final String DESCRIPTION_LONG_TAG = "longDescription";
    static final String MULTIMEDIA_TAG = "multimedia";
    static final String NAME_SHORT_TAG = "shortName";
    static final String NAME_MEDIUM_TAG = "mediumName";
    static final String NAME_LONG_TAG = "longName";
    static final String MEDIADESCRIPTION_TAG = "mediaDescription";
    static final String BEARER_TAG = "bearer";
    static final String TYPE_ATTR = "type";
    static final String MIMEVALUE_ATTR = "mimeValue";
    static final String URL_ATTR = "url";
    static final String URI_ATTR = "uri";
    static final String WIDTH_ATTR = "width";
    static final String HEIGHT_ATTR = "height";
    static final String ID_ATTR = "id";
    static final String SHORT_ID_ATTR = "shortId";
    static final String MIME_ATTR = "mime";
    static final String COST_ATTR = "cost";
    static final String DESCRIPTION_ATTR = "description";
    static final String EXPIRYTIME_ATTR = "expiryTime";
    static final String KEYWORDS_TAG = "keywords";
    static final String LINK_TAG = "link";
    static final String GEOLOCATION_TAG = "geolocation";
    static final String GEOLOCATION_COUNTRY_TAG = "country";
    static final String GEOLOCATION_POINT_TAG = "point";
    static final String GEOLOCATION_POLYGON_TAG = "polygon";
    static final String RADIODNS_TAG = "radiodns";
    static final String RADIODNS_FQDN_ATTR = "fqdn";
    static final String RADIODNS_SID_ATTR = "serviceIdentifier";
    static final String GENRE_TAG = "genre";
    static final String HREF_ATTR = "href";
    static final String APPLICATION_TAG = "application";
    static final String CONTROL_ATTR = "control";
    static final String APPID_ATTR = "applicationID";
    static final String APPPRIO_ATTR = "applicationPriority";
    static final String APPSCOPE_TAG = "applicationScope";
    static final String SERVICESCOPE_TAG = "serviceScope";
    String mDocumentLanguage;
    
    RadioEpgParser() {
        this.mDocumentLanguage = "en";
    }
    
    Name parseName(final XmlPullParser parser, final String nameTag, final NameType type) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, nameTag);
        String nameLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, XMLLANG_ATTR);
        if (nameLang == null) {
            nameLang = this.mDocumentLanguage;
        }
        final Name retName = new Name(type, nameLang, this.readTagText(parser));
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, nameTag);
        return retName;
    }
    
    MediaDescription parseMediaDescription(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, MEDIADESCRIPTION_TAG);
        final MediaDescription desc = new MediaDescription();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() !=  XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals(DESCRIPTION_SHORT_TAG) || name.equals(DESCRIPTION_LONG_TAG)) {
                desc.addDescription(this.parseDescription(parser, name, DescriptionType.DESCRIPTION_SHORT));
            }
            else {
                if (!name.equals(MULTIMEDIA_TAG)) {
                    continue;
                }
                final Multimedia mm = this.parseMultimedia(parser);
                if (mm == null) {
                    continue;
                }
                desc.setMultimedia(mm);
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, MEDIADESCRIPTION_TAG);
        return desc;
    }
    
    Description parseDescription(final XmlPullParser parser, final String descTag, final DescriptionType type) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, descTag);
        String descriptionLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, XMLLANG_ATTR);
        if (descriptionLang == null) {
            descriptionLang = this.mDocumentLanguage;
        }
        final String descriptionText = this.readTagText(parser);
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, descTag);
        return new Description(type, descriptionLang, descriptionText);
    }
    
    Bearer parseBearer(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, BEARER_TAG);
        final String bearerIdString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, ID_ATTR);
        String bearerMimeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, MIMEVALUE_ATTR);
        final String bearerCostString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, COST_ATTR);
        Bearer bearer = null;
        int bearerCost = -1;
        if (bearerMimeString == null) {
            bearerMimeString = "";
        }
        if (bearerCostString != null) {
            try {
                bearerCost = Integer.parseInt(bearerCostString.trim());
            } catch (Exception e) {
                if(DEBUG) {
                    Log.d(TAG, BEARER_TAG + ":'" + bearerCostString + "'");
                    e.printStackTrace();
                }
            }
        }
        if (bearerIdString != null) {
            bearer = new Bearer(bearerIdString, bearerCost, bearerMimeString, 0);
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, BEARER_TAG);
        return bearer;
    }
    
    Link parseLink(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, LINK_TAG);
        Link retLink = null;
        final String linkUri = parser.getAttributeValue(RadioEpgParser.NAMESPACE, URI_ATTR);
        if (linkUri != null) {
            final String linkMimeVal = parser.getAttributeValue(RadioEpgParser.NAMESPACE, MIMEVALUE_ATTR);
            final String linkLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, XMLLANG_ATTR);
            final String linkDesc = parser.getAttributeValue(RadioEpgParser.NAMESPACE, DESCRIPTION_ATTR);
            final String linkExpiry = parser.getAttributeValue(RadioEpgParser.NAMESPACE, EXPIRYTIME_ATTR);
            retLink = new Link(linkUri.trim(), (linkMimeVal != null) ? linkMimeVal.trim() : "", (linkLang != null) ? linkLang.trim() : this.mDocumentLanguage, (linkDesc != null) ? linkDesc.trim() : "", (linkExpiry != null) ? linkExpiry.trim() : "");
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, LINK_TAG);
        return retLink;
    }
    
    GeoLocation parseGeoLocation(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_TAG);
        final GeoLocation retLoc = new GeoLocation();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals(GEOLOCATION_COUNTRY_TAG)) {
                retLoc.addCountryString(this.readTagText(parser));
            }
            else if (name.equals(GEOLOCATION_POINT_TAG)) {
                final GeoLocationPoint point = this.parseGeoLocationPoint(parser);
                if (point == null) {
                    continue;
                }
                retLoc.addLocationPoint(point);
            }
            else if (name.equals(GEOLOCATION_POLYGON_TAG)) {
                final GeoLocationPolygon poly = this.parseGeoLocationPolygon(parser);
                if (poly == null) {
                    continue;
                }
                retLoc.addLocationPolygon(poly);
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_TAG);
        return retLoc;
    }
    
    GeoLocationPolygon parseGeoLocationPolygon(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_POLYGON_TAG);
        GeoLocationPolygon retPoly = null;
        final String[] polySplit = this.readTagText(parser).split(",");
        final ArrayList<GeoLocationPoint> polyPoints = new ArrayList<GeoLocationPoint>();
        for (final String latLong : polySplit) {
            final String[] pointSplit = latLong.split("\\s+");
            if (pointSplit.length == 2) {
                polyPoints.add(new GeoLocationPoint(Double.parseDouble(pointSplit[0].trim()), Double.parseDouble(pointSplit[1].trim())));
            }
        }
        if (!polyPoints.isEmpty()) {
            retPoly = new GeoLocationPolygon(polyPoints);
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_POLYGON_TAG);
        return retPoly;
    }
    
    GeoLocationPoint parseGeoLocationPoint(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_POINT_TAG);
        GeoLocationPoint retPoint = null;
        final String tag = this.readTagText(parser);
        // some separate with ", " instead of only " "
        final String correctedTag = tag.replaceAll(",", "");
        final String[] pointSplit = correctedTag.split("\\s+");
        if (pointSplit.length == 2) {
            try {
                retPoint = new GeoLocationPoint(Double.parseDouble(pointSplit[0].trim()), Double.parseDouble(pointSplit[1].trim()));
            } catch (Exception e) {
                if(DEBUG) {
                    Log.d(TAG, GEOLOCATION_POINT_TAG + ":'" + tag + "'");
                    e.printStackTrace();
                }
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, GEOLOCATION_POINT_TAG);
        return retPoint;
    }
    
    Multimedia parseMultimedia(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, MULTIMEDIA_TAG);
        String multimediaLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, XMLLANG_ATTR);
        if (multimediaLang == null) {
            multimediaLang = this.mDocumentLanguage;
        }
        final MultimediaType mmType = MultimediaType.fromTypeString(parser.getAttributeValue(RadioEpgParser.NAMESPACE, TYPE_ATTR));
        final String multimediaUrl = parser.getAttributeValue(RadioEpgParser.NAMESPACE, URL_ATTR);
        String multimediaMime = parser.getAttributeValue(RadioEpgParser.NAMESPACE, MIMEVALUE_ATTR);
        String multimediaWidth = parser.getAttributeValue(RadioEpgParser.NAMESPACE, WIDTH_ATTR);
        String multimediaHeight = parser.getAttributeValue(RadioEpgParser.NAMESPACE, HEIGHT_ATTR);
        Multimedia mmRet = null;
        if (multimediaUrl != null) {
            if (mmType != MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED && multimediaMime == null) {
                multimediaMime = "";
            }
            if (mmType == MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED) {
                int mmWidth = -1;
                int mmHeight = -1;
                if (multimediaWidth != null) {
                    // some use "1.920" instead of "1920"
                    multimediaWidth = multimediaWidth.replaceAll("\\p{Punct}", "");
                    try {
                        mmWidth = Integer.parseInt(multimediaWidth.trim());

                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, WIDTH_ATTR + ":'" + multimediaWidth + "'");
                            e.printStackTrace();
                        }
                    }
                }
                if (multimediaHeight != null) {
                    // some use "1.920" instead of "1920"
                    multimediaHeight = multimediaHeight.replaceAll("\\p{Punct}", "");
                    try {
                        mmHeight = Integer.parseInt(multimediaHeight.trim());
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, HEIGHT_ATTR + ":'" + multimediaHeight + "'");
                            e.printStackTrace();
                        }
                    }
                }
                mmRet = new Multimedia(mmType, multimediaLang, multimediaUrl, multimediaMime, mmWidth, mmHeight);
            }
            else {
                mmRet = new Multimedia(mmType, multimediaLang, multimediaUrl);
            }
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, MULTIMEDIA_TAG);
        return mmRet;
    }
    
    Genre parseGenre(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, GENRE_TAG);
        Genre retGenre = null;
        final String hrefString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, HREF_ATTR);
        final String typeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, TYPE_ATTR);
        if (hrefString != null) {
            if (typeString != null) {
                retGenre = new Genre(hrefString.trim(), typeString.trim());
            }
            else {
                retGenre = new Genre(hrefString.trim());
            }
            if (retGenre.getGenre() == null || retGenre.getGenre().isEmpty()) {
                retGenre = null;
            }
        }
        parser.next();
        if (parser.getEventType() == 4) {
            this.readTagText(parser);
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, GENRE_TAG);
        return retGenre;
    }
    
    ServiceScope parseApplicationScope(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, APPSCOPE_TAG);
        final String scopeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, ID_ATTR);
        ServiceScope appScope = null;
        if (scopeString != null) {
            appScope = new ServiceScope(scopeString);
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, APPSCOPE_TAG);
        return appScope;
    }
    
    ServiceScope parseServiceScope(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, RadioEpgParser.NAMESPACE, SERVICESCOPE_TAG);
        final String scopeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, ID_ATTR);
        ServiceScope appScope = null;
        if (scopeString != null) {
            appScope = new ServiceScope(scopeString);
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgParser.NAMESPACE, SERVICESCOPE_TAG);
        return appScope;
    }
    
    String readTagText(final XmlPullParser parser) throws IOException, XmlPullParserException {
        String redtext = "";
        if (parser.next() == 4) {
            redtext = parser.getText().trim();
            parser.nextTag();
        }
        return redtext;
    }
    
    void skip(final XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG: {
                    --depth;
                    continue;
                }
                case XmlPullParser.START_TAG: {
                    ++depth;
                    continue;
                }
            }
        }
    }
    
    static {
        NAMESPACE = null;
    }
}
