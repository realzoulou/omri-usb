package eu.hradio.core.radiodns;

import android.util.*;
import org.xmlpull.v1.*;
import java.io.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import eu.hradio.core.radiodns.radioepg.radiodns.*;
import eu.hradio.core.radiodns.radioepg.genre.*;
import eu.hradio.core.radiodns.radioepg.serviceinformation.*;

public class RadioEpgSiParser extends RadioEpgParser
{
    private static final String TAG = "REpgSiParser";
    private RadioEpgServiceInformation mParsedSi;
    
    RadioEpgServiceInformation parse(final InputStream siDataStream) throws IOException {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(siDataStream, RadioEpgSiParser.NAMESPACE);
            parser.nextTag();
            this.mParsedSi = this.parseRoot(parser);
            while (parser.next() != 3) {
                final String tagName = parser.getName();
                if (parser.getEventType() != 2) {
                    continue;
                }
                if (tagName.equals("services")) {
                    this.parseServices(parser);
                }
                else {
                    this.skip(parser);
                }
            }
        }
        catch (XmlPullParserException ex) {}
        finally {
            siDataStream.close();
        }
        return this.mParsedSi;
    }
    
    private void parseServices(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgSiParser.NAMESPACE, "services");
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("serviceProvider")) {
                this.mParsedSi.setServiceProvider(this.parseServiceProvider(parser));
            }
            else if (tagName.equals("service")) {
                this.mParsedSi.addService(this.parseService(parser));
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioEpgSiParser.NAMESPACE, "services");
    }
    
    private RadioEpgServiceInformation parseRoot(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgSiParser.NAMESPACE, "serviceInformation");
        final String creationTime = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "creationTime");
        final String originator = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "originator");
        final String xmlLang = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "xml:lang");
        final String version = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "version");
        final String termsUrl = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "terms");
        if (xmlLang != null) {
            this.mDocumentLanguage = xmlLang;
        }
        int siVersion = 1;
        if (version != null) {
            try {
                siVersion = Integer.parseInt(version.trim());
            }
            catch (NumberFormatException ex) {}
        }
        return new RadioEpgServiceInformation((creationTime == null) ? "" : creationTime.trim(), (originator == null) ? "" : originator.trim(), (xmlLang == null) ? "en" : xmlLang.trim(), siVersion, (termsUrl != null) ? termsUrl : "");
    }
    
    private Service parseService(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgSiParser.NAMESPACE, "service");
        final Service retService = new Service();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("shortName")) {
                retService.addName(this.parseName(parser, "shortName", NameType.NAME_SHORT));
            }
            else if (name.equals("mediumName")) {
                retService.addName(this.parseName(parser, "mediumName", NameType.NAME_MEDIUM));
            }
            else if (name.equals("longName")) {
                retService.addName(this.parseName(parser, "longName", NameType.NAME_LONG));
            }
            else if (name.equals("mediaDescription")) {
                retService.addMediaDescription(this.parseMediaDescription(parser));
            }
            else if (name.equals("keywords")) {
                retService.setKeywords(this.readTagText(parser));
            }
            else if (name.equals("link")) {
                final Link provLink = this.parseLink(parser);
                if (provLink == null) {
                    continue;
                }
                retService.addLink(provLink);
            }
            else if (name.equals("bearer")) {
                final Bearer bearer = this.parseBearer(parser);
                if (bearer == null) {
                    continue;
                }
                retService.addBearer(bearer);
            }
            else if (name.equals("radiodns")) {
                final RadioDns rdns = this.parseRadioDns(parser);
                if (rdns == null) {
                    continue;
                }
                retService.setRadioDns(rdns);
            }
            else if (name.equals("geolocation")) {
                retService.setGeoLocation(this.parseGeoLocation(parser));
            }
            else if (name.equals("genre")) {
                final Genre genre = this.parseGenre(parser);
                if (genre == null) {
                    continue;
                }
                retService.addGenre(genre);
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioEpgSiParser.NAMESPACE, "service");
        return retService;
    }
    
    private RadioDns parseRadioDns(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgSiParser.NAMESPACE, "radiodns");
        RadioDns retDns = null;
        final String fqdn = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "fqdn");
        final String sid = parser.getAttributeValue(RadioEpgSiParser.NAMESPACE, "serviceIdentifier");
        if (fqdn != null && sid != null) {
            retDns = new RadioDns(fqdn, sid);
        }
        parser.nextTag();
        parser.require(3, RadioEpgSiParser.NAMESPACE, "radiodns");
        return retDns;
    }
    
    private ServiceProvider parseServiceProvider(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgSiParser.NAMESPACE, "serviceProvider");
        final ServiceProvider provider = new ServiceProvider();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("shortName")) {
                provider.addName(this.parseName(parser, "shortName", NameType.NAME_SHORT));
            }
            else if (name.equals("mediumName")) {
                provider.addName(this.parseName(parser, "mediumName", NameType.NAME_MEDIUM));
            }
            else if (name.equals("longName")) {
                provider.addName(this.parseName(parser, "longName", NameType.NAME_LONG));
            }
            else if (name.equals("mediaDescription")) {
                provider.addMediaDescription(this.parseMediaDescription(parser));
            }
            else if (name.equals("keywords")) {
                provider.setKeywords(this.readTagText(parser));
            }
            else if (name.equals("link")) {
                final Link provLink = this.parseLink(parser);
                if (provLink == null) {
                    continue;
                }
                provider.addLink(provLink);
            }
            else if (name.equals("geolocation")) {
                provider.setGeolocation(this.parseGeoLocation(parser));
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioEpgSiParser.NAMESPACE, "serviceProvider");
        return provider;
    }
}
