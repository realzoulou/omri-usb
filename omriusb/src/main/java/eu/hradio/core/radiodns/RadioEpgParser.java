package eu.hradio.core.radiodns;

import eu.hradio.core.radiodns.radioepg.name.*;
import java.io.*;
import org.xmlpull.v1.*;
import eu.hradio.core.radiodns.radioepg.mediadescription.*;
import eu.hradio.core.radiodns.radioepg.description.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import eu.hradio.core.radiodns.radioepg.geolocation.*;
import java.util.*;
import eu.hradio.core.radiodns.radioepg.multimedia.*;
import eu.hradio.core.radiodns.radioepg.genre.*;
import eu.hradio.core.radiodns.radioepg.scope.*;

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
        parser.require(2, RadioEpgParser.NAMESPACE, nameTag);
        String nameLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "xml:lang");
        if (nameLang == null) {
            nameLang = this.mDocumentLanguage;
        }
        final Name retName = new Name(type, nameLang, this.readTagText(parser));
        parser.require(3, RadioEpgParser.NAMESPACE, nameTag);
        return retName;
    }
    
    MediaDescription parseMediaDescription(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, "mediaDescription");
        final MediaDescription desc = new MediaDescription();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("shortDescription") || name.equals("longDescription")) {
                desc.addDescription(this.parseDescription(parser, name, DescriptionType.DESCRIPTION_SHORT));
            }
            else {
                if (!name.equals("multimedia")) {
                    continue;
                }
                final Multimedia mm = this.parseMultimedia(parser);
                if (mm == null) {
                    continue;
                }
                desc.setMultimedia(mm);
            }
        }
        parser.require(3, RadioEpgParser.NAMESPACE, "mediaDescription");
        return desc;
    }
    
    Description parseDescription(final XmlPullParser parser, final String descTag, final DescriptionType type) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, descTag);
        String descriptionLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "xml:lang");
        if (descriptionLang == null) {
            descriptionLang = this.mDocumentLanguage;
        }
        final String descriptionText = this.readTagText(parser);
        parser.require(3, RadioEpgParser.NAMESPACE, descTag);
        return new Description(type, descriptionLang, descriptionText);
    }
    
    Bearer parseBearer(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, "bearer");
        final String bearerIdString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "id");
        String bearerMimeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "mimeValue");
        final String bearerCostString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "cost");
        Bearer bearer = null;
        int bearerCost = -1;
        if (bearerMimeString == null) {
            bearerMimeString = "";
        }
        if (bearerCostString != null) {
            try {
                bearerCost = Integer.parseInt(bearerCostString.trim());
            }
            catch (NumberFormatException ex) {}
        }
        if (bearerIdString != null) {
            bearer = new Bearer(bearerIdString, bearerCost, bearerMimeString, 0);
        }
        parser.nextTag();
        parser.require(3, RadioEpgParser.NAMESPACE, "bearer");
        return bearer;
    }
    
    Link parseLink(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgParser.NAMESPACE, "link");
        Link retLink = null;
        final String linkUri = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "uri");
        if (linkUri != null) {
            final String linkMimeVal = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "mimeValue");
            final String linkLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "xml:lang");
            final String linkDesc = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "description");
            final String linkExpiry = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "expiryTime");
            retLink = new Link(linkUri.trim(), (linkMimeVal != null) ? linkMimeVal.trim() : "", (linkLang != null) ? linkLang.trim() : this.mDocumentLanguage, (linkDesc != null) ? linkDesc.trim() : "", (linkExpiry != null) ? linkExpiry.trim() : "");
        }
        parser.nextTag();
        parser.require(3, RadioEpgParser.NAMESPACE, "link");
        return retLink;
    }
    
    GeoLocation parseGeoLocation(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgParser.NAMESPACE, "geolocation");
        final GeoLocation retLoc = new GeoLocation();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("country")) {
                retLoc.addCountryString(this.readTagText(parser));
            }
            else if (name.equals("point")) {
                final GeoLocationPoint point = this.parseGeoLocationPoint(parser);
                if (point == null) {
                    continue;
                }
                retLoc.addLocationPoint(point);
            }
            else if (name.equals("polygon")) {
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
        parser.require(3, RadioEpgParser.NAMESPACE, "geolocation");
        return retLoc;
    }
    
    GeoLocationPolygon parseGeoLocationPolygon(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgParser.NAMESPACE, "polygon");
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
        parser.require(3, RadioEpgParser.NAMESPACE, "polygon");
        return retPoly;
    }
    
    GeoLocationPoint parseGeoLocationPoint(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgParser.NAMESPACE, "point");
        GeoLocationPoint retPoint = null;
        final String[] pointSplit = this.readTagText(parser).split("\\s+");
        if (pointSplit.length == 2) {
            retPoint = new GeoLocationPoint(Double.parseDouble(pointSplit[0].trim()), Double.parseDouble(pointSplit[1].trim()));
        }
        parser.require(3, RadioEpgParser.NAMESPACE, "point");
        return retPoint;
    }
    
    Multimedia parseMultimedia(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, "multimedia");
        String multimediaLang = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "xml:lang");
        if (multimediaLang == null) {
            multimediaLang = this.mDocumentLanguage;
        }
        final MultimediaType mmType = MultimediaType.fromTypeString(parser.getAttributeValue(RadioEpgParser.NAMESPACE, "type"));
        final String multimediaUrl = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "url");
        String multimediaMime = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "mimeValue");
        final String multimediaWidth = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "width");
        final String multimediaHeight = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "height");
        Multimedia mmRet = null;
        if (multimediaUrl != null) {
            if (mmType != MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED && multimediaMime == null) {
                multimediaMime = "";
            }
            if (mmType == MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED) {
                int mmWidth = -1;
                int mmHeight = -1;
                if (multimediaWidth != null) {
                    try {
                        mmWidth = Integer.parseInt(multimediaWidth.trim());
                    }
                    catch (NumberFormatException ex) {}
                }
                if (multimediaHeight != null) {
                    try {
                        mmHeight = Integer.parseInt(multimediaHeight.trim());
                    }
                    catch (NumberFormatException ex2) {}
                }
                mmRet = new Multimedia(mmType, multimediaLang, multimediaUrl, multimediaMime, mmWidth, mmHeight);
            }
            else {
                mmRet = new Multimedia(mmType, multimediaLang, multimediaUrl);
            }
        }
        parser.nextTag();
        parser.require(3, RadioEpgParser.NAMESPACE, "multimedia");
        return mmRet;
    }
    
    Genre parseGenre(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgParser.NAMESPACE, "genre");
        Genre retGenre = null;
        final String hrefString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "href");
        final String typeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "type");
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
        parser.require(3, RadioEpgParser.NAMESPACE, "genre");
        return retGenre;
    }
    
    ServiceScope parseApplicationScope(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, "applicationScope");
        final String scopeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "id");
        ServiceScope appScope = null;
        if (scopeString != null) {
            appScope = new ServiceScope(scopeString);
        }
        parser.nextTag();
        parser.require(3, RadioEpgParser.NAMESPACE, "applicationScope");
        return appScope;
    }
    
    ServiceScope parseServiceScope(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, RadioEpgParser.NAMESPACE, "serviceScope");
        final String scopeString = parser.getAttributeValue(RadioEpgParser.NAMESPACE, "id");
        ServiceScope appScope = null;
        if (scopeString != null) {
            appScope = new ServiceScope(scopeString);
        }
        parser.nextTag();
        parser.require(3, RadioEpgParser.NAMESPACE, "serviceScope");
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
        if (parser.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case 3: {
                    --depth;
                    continue;
                }
                case 2: {
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
