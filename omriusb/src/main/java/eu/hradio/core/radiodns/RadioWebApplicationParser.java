package eu.hradio.core.radiodns;

import android.util.*;
import org.xmlpull.v1.*;
import java.io.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.scope.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;

class RadioWebApplicationParser extends RadioEpgParser
{
    private static final String TAG = "RWebAppParser";
    private RadioWebApplicationInformationList mParsedAil;
    private static final String NAMESPACE;
    private static final String APPLICATIONINFORMATION_TAG = "applicationInformation";
    
    RadioWebApplicationInformationList parse(final InputStream rwebDataStream) throws IOException {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(rwebDataStream, (String)null);
            parser.nextTag();
            this.mParsedAil = this.parseRoot(parser);
            while (parser.next() != 3) {
                final String tagName = parser.getName();
                if (parser.getEventType() != 2) {
                    continue;
                }
                if (tagName.equals("application")) {
                    this.mParsedAil.addApplication(this.parseApplication(parser));
                }
                else {
                    this.skip(parser);
                }
            }
        }
        catch (XmlPullParserException ex) {}
        finally {
            rwebDataStream.close();
        }
        return this.mParsedAil;
    }
    
    private RadioWebApplicationInformationList parseRoot(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioWebApplicationParser.NAMESPACE, "applicationInformation");
        final String creationTime = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "creationTime");
        final String originator = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "originator");
        final String xmlLang = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "xml:lang");
        if (xmlLang != null) {
            this.mDocumentLanguage = xmlLang;
        }
        return new RadioWebApplicationInformationList((creationTime == null) ? "" : creationTime.trim(), (originator == null) ? "" : originator.trim(), (xmlLang == null) ? "en" : xmlLang.trim());
    }
    
    private RadioWebApplication parseApplication(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioWebApplicationParser.NAMESPACE, "application");
        final String controlCode = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "control");
        final String appId = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "applicationID");
        final String appPrio = parser.getAttributeValue(RadioWebApplicationParser.NAMESPACE, "applicationPriority");
        int appIdInt = -1;
        if (appId != null) {
            try {
                appIdInt = Integer.parseInt(appId.trim());
            }
            catch (NumberFormatException ex) {}
        }
        int appPrioInt = -1;
        if (appPrio != null) {
            try {
                appPrioInt = Integer.parseInt(appPrio.trim());
            }
            catch (NumberFormatException ex2) {}
        }
        final RadioWebApplication radioWebApp = new RadioWebApplication(RadioWebApplicationControl.getControl(controlCode), appIdInt, appPrioInt);
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String name = parser.getName();
            if (name.equals("applicationScope")) {
                final ServiceScope appScope = this.parseApplicationScope(parser);
                if (appScope == null) {
                    continue;
                }
                radioWebApp.addServiceScope(appScope);
            }
            else if (name.equals("shortName")) {
                radioWebApp.addName(this.parseName(parser, "shortName", NameType.NAME_SHORT));
            }
            else if (name.equals("mediumName")) {
                radioWebApp.addName(this.parseName(parser, "mediumName", NameType.NAME_MEDIUM));
            }
            else if (name.equals("longName")) {
                radioWebApp.addName(this.parseName(parser, "longName", NameType.NAME_LONG));
            }
            else if (name.equals("mediaDescription")) {
                radioWebApp.addMediaDescription(this.parseMediaDescription(parser));
            }
            else if (name.equals("bearer")) {
                final Bearer bearer = this.parseBearer(parser);
                if (bearer == null) {
                    continue;
                }
                radioWebApp.addBearer(bearer);
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioWebApplicationParser.NAMESPACE, "application");
        return radioWebApp;
    }
    
    static {
        NAMESPACE = null;
    }
}
