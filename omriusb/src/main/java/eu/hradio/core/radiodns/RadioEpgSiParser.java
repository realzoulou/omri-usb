package eu.hradio.core.radiodns;

import android.util.Log;
import android.util.Xml;

import org.omri.BuildConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import eu.hradio.core.radiodns.radioepg.bearer.Bearer;
import eu.hradio.core.radiodns.radioepg.genre.Genre;
import eu.hradio.core.radiodns.radioepg.geolocation.GeoLocation;
import eu.hradio.core.radiodns.radioepg.link.Link;
import eu.hradio.core.radiodns.radioepg.name.Name;
import eu.hradio.core.radiodns.radioepg.name.NameType;
import eu.hradio.core.radiodns.radioepg.radiodns.RadioDns;
import eu.hradio.core.radiodns.radioepg.serviceinformation.Service;
import eu.hradio.core.radiodns.radioepg.serviceinformation.ServiceProvider;

public class RadioEpgSiParser extends RadioEpgParser
{
    private static final String TAG = "REpgSiParser";

    RadioEpgServiceInformation parse(final InputStream siDataStream) throws IOException {
        RadioEpgServiceInformation parsedSi = null;
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(siDataStream, RadioEpgSiParser.NAMESPACE);
            parser.nextTag();
            parsedSi = this.parseRoot(parser);
            while (parser.next() != XmlPullParser.END_TAG) {
                final String tagName = parser.getName();
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                if (tagName.equals(SERVICES_TAG)) {
                    this.parseServices(parser, parsedSi);
                }
                else {
                    this.skip(parser);
                }
            }
        }
        catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
        }
        finally {
            siDataStream.close();
        }
        return parsedSi;
    }
    
    private void parseServices(final XmlPullParser parser, RadioEpgServiceInformation parsedSi) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgSiParser.NAMESPACE, SERVICES_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals(SERVICEPROVIDER_TAG)) {
                parsedSi.setServiceProvider(this.parseServiceProvider(parser));
            }
            else if (tagName.equals(SERVICE_TAG)) {
                parsedSi.addService(this.parseService(parser));
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgSiParser.NAMESPACE, SERVICES_TAG);
    }
    
    private RadioEpgServiceInformation parseRoot(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgSiParser.NAMESPACE, SERVICEINFORMATION_TAG);
        final String creationTime = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, CREATIONTIME_ATTR);
        final String originator = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, ORIGINATOR_ATTR);
        final String xmlLang = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, XMLLANG_ATTR);
        final String version = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, VERSION_ATTR);
        final String termsUrl = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, TERMS_ATTR);
        if (xmlLang != null) {
            this.mDocumentLanguage = xmlLang;
        }
        int siVersion = 1;
        if (version != null) {
            try {
                siVersion = Integer.parseInt(version.trim());
            }
            catch (Exception ex) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, VERSION_ATTR + ": invalid '" + version + "'");
                }
            }
        }
        return new RadioEpgServiceInformation((creationTime == null) ? "" : creationTime.trim(), (originator == null) ? "" : originator.trim(), (xmlLang == null) ? "en" : xmlLang.trim(), siVersion, (termsUrl != null) ? termsUrl : "");
    }
    
    private Service parseService(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgSiParser.NAMESPACE, SERVICE_TAG);
        final Service retService = new Service();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            switch (name) {
                case NAME_SHORT_TAG:
                    final Name shortName = this.parseName(parser, NAME_SHORT_TAG, NameType.NAME_SHORT);
                    if (shortName != null) {
                        retService.addName(shortName);
                    }
                    break;
                case NAME_MEDIUM_TAG:
                    final Name mediumName = this.parseName(parser, NAME_MEDIUM_TAG, NameType.NAME_MEDIUM);
                    if (mediumName != null) {
                        retService.addName(mediumName);
                    }
                    break;
                case NAME_LONG_TAG:
                    final Name longName = this.parseName(parser, NAME_LONG_TAG, NameType.NAME_LONG);
                    if (longName != null) {
                        retService.addName(longName);
                    }
                    break;
                case MEDIADESCRIPTION_TAG:
                    final eu.hradio.core.radiodns.radioepg.mediadescription.MediaDescription mediaDescription = this.parseMediaDescription(parser);
                    if (mediaDescription != null) {
                        retService.addMediaDescription(mediaDescription);
                    }
                    break;
                case KEYWORDS_TAG:
                    final String keywords = this.readTagText(parser);
                    if (keywords != null) {
                        retService.setKeywords(keywords);
                    }
                    break;
                case LINK_TAG:
                    final Link provLink = this.parseLink(parser);
                    if (provLink != null) {
                        retService.addLink(provLink);
                    }
                    break;
                case BEARER_TAG:
                    final Bearer bearer = this.parseBearer(parser);
                    if (bearer != null) {
                        retService.addBearer(bearer);
                    }
                    break;
                case RADIODNS_TAG:
                    final RadioDns rdns = this.parseRadioDns(parser);
                    if (rdns != null) {
                        retService.setRadioDns(rdns);
                    }
                    break;
                case GEOLOCATION_TAG:
                    final GeoLocation geoLocation = this.parseGeoLocation(parser);
                    if (geoLocation != null) {
                        retService.setGeoLocation(geoLocation);
                    }
                    break;
                case GENRE_TAG:
                    final Genre genre = this.parseGenre(parser);
                    if (genre != null) {
                        retService.addGenre(genre);
                    }
                    break;
                default:
                    this.skip(parser);
                    break;
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgSiParser.NAMESPACE, SERVICE_TAG);
        return retService;
    }
    
    private RadioDns parseRadioDns(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgSiParser.NAMESPACE, RADIODNS_TAG);
        RadioDns retDns = null;
        final String fqdn = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, RADIODNS_FQDN_ATTR);
        final String sid = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, RADIODNS_SID_ATTR);
        if (fqdn != null && sid != null) {
            retDns = new RadioDns(fqdn, sid);
        }
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, RadioEpgSiParser.NAMESPACE, RADIODNS_TAG);
        return retDns;
    }
    
    private ServiceProvider parseServiceProvider(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, RadioEpgSiParser.NAMESPACE, SERVICEPROVIDER_TAG);
        final ServiceProvider provider = new ServiceProvider();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            switch (name) {
                case NAME_SHORT_TAG:
                    final Name shortName = this.parseName(parser, NAME_SHORT_TAG, NameType.NAME_SHORT);
                    if (shortName != null) {
                        provider.addName(shortName);
                    }
                    break;
                case NAME_MEDIUM_TAG:
                    final Name mediumName = this.parseName(parser, NAME_MEDIUM_TAG, NameType.NAME_MEDIUM);
                    if (mediumName != null) {
                        provider.addName(mediumName);
                    }
                    break;
                case NAME_LONG_TAG:
                    final Name longName = this.parseName(parser, NAME_LONG_TAG, NameType.NAME_LONG);
                    if (longName != null) {
                        provider.addName(longName);
                    }
                    break;
                case MEDIADESCRIPTION_TAG:
                    final eu.hradio.core.radiodns.radioepg.mediadescription.MediaDescription mediaDescription = this.parseMediaDescription(parser);
                    if (mediaDescription != null) {
                        provider.addMediaDescription(mediaDescription);
                    }
                    break;
                case KEYWORDS_TAG:
                    final String keywords = this.readTagText(parser);
                    if (keywords != null) {
                        provider.setKeywords(keywords);
                    }
                    break;
                case LINK_TAG:
                    final Link provLink = this.parseLink(parser);
                    if (provLink != null) {
                        provider.addLink(provLink);
                    }
                    break;
                case GEOLOCATION_TAG:
                    final GeoLocation geoLocation = this.parseGeoLocation(parser);
                    if (geoLocation != null) {
                        provider.setGeolocation(geoLocation);
                    }
                    break;
                default:
                    this.skip(parser);
                    break;
            }
        }
        parser.require(XmlPullParser.END_TAG, RadioEpgSiParser.NAMESPACE, SERVICEPROVIDER_TAG);
        return provider;
    }
}
