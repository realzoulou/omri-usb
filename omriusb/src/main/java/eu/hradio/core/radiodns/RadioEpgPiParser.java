package eu.hradio.core.radiodns;

import android.util.*;
import org.xmlpull.v1.*;
import java.io.*;
import eu.hradio.core.radiodns.radioepg.scope.*;
import eu.hradio.core.radiodns.radioepg.crid.*;
import eu.hradio.core.radiodns.radioepg.name.*;
import eu.hradio.core.radiodns.radioepg.keywords.*;
import eu.hradio.core.radiodns.radioepg.genre.*;
import eu.hradio.core.radiodns.radioepg.link.*;
import eu.hradio.core.radiodns.radioepg.bearer.*;
import eu.hradio.core.radiodns.radioepg.programmeinformation.*;
import java.util.*;
import eu.hradio.core.radiodns.radioepg.time.*;

public class RadioEpgPiParser extends RadioEpgParser
{
    private static final String TAG = "REpgPiParser";
    private RadioEpgProgrammeInformation mParsedPi;
    private static final String EPG_TAG = "epg";
    private static final String SCHEDULE_TAG = "schedule";
    private static final String SCOPE_TAG = "scope";
    private static final String START_TIME_ATTR = "startTime";
    private static final String STOP_TIME_ATTR = "stopTime";
    private static final String PROGRAMME_TAG = "programme";
    private static final String TIME_TAG = "time";
    private static final String RELATIVE_TIME_TAG = "relativeTime";
    private static final String PROGRAMME_EVENT_TAG = "programmeEvent";
    private static final String LOCATION_TAG = "location";
    private static final String ONDEMAND_TAG = "onDemand";
    private static final String PRESENTATIONTIME_TAG = "presentationTime";
    private static final String ACQUISITIONTIME_TAG = "acquisitionTime";
    private static final String START_ATTR = "start";
    private static final String END_ATTR = "end";
    private static final String DURATION_ATTR = "duration";
    private static final String ACTUALTIME_ATTR = "actualTime";
    private static final String ACTUALDURATION_ATTR = "actualDuration";
    private static final String TIME_ATTR = "time";
    private static final String RECOMMENDATION_ATTR = "recommendation";
    private static final String BROADCAST_ATTR = "broadcast";
    
    RadioEpgProgrammeInformation parse(final InputStream piDataStream) throws IOException {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(piDataStream, RadioEpgPiParser.NAMESPACE);
            parser.nextTag();
            this.mParsedPi = new RadioEpgProgrammeInformation();
            while (parser.next() != 3) {
                final String tagName = parser.getName();
                if (parser.getEventType() != 2) {
                    continue;
                }
                if (tagName.equals("schedule")) {
                    this.mParsedPi.addSchedule(this.parseSchedule(parser));
                }
                else {
                    this.skip(parser);
                }
            }
        }
        catch (XmlPullParserException ex) {}
        finally {
            piDataStream.close();
        }
        return this.mParsedPi;
    }
    
    private Schedule parseSchedule(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "schedule");
        String creationTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "creationTime");
        String originatorString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "originator");
        final String versionString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "version");
        String langString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "xml:lang");
        if (creationTimeString == null) {
            creationTimeString = "";
        }
        if (originatorString == null) {
            originatorString = "";
        }
        int version = 1;
        if (versionString != null) {
            try {
                version = Integer.parseInt(versionString.trim());
            }
            catch (NumberFormatException ex) {}
        }
        if (langString == null) {
            langString = this.mDocumentLanguage;
        }
        final Schedule retSchedule = new Schedule(creationTimeString, originatorString, version, langString);
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("scope")) {
                final Scope schedScope = this.parseScope(parser);
                if (schedScope == null) {
                    continue;
                }
                retSchedule.setScope(schedScope);
            }
            else if (tagName.equals("programme")) {
                final Programme prog = this.parseProgramme(parser);
                if (prog == null) {
                    continue;
                }
                retSchedule.addProgramme(prog);
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "schedule");
        return retSchedule;
    }
    
    private Scope parseScope(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "scope");
        final String startTime = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "startTime");
        final String stopTime = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "stopTime");
        Scope retScope = null;
        if (startTime != null && stopTime != null) {
            retScope = new Scope(startTime, stopTime);
            while (parser.next() != 3) {
                if (parser.getEventType() != 2) {
                    continue;
                }
                final String tagName = parser.getName();
                if (tagName.equals("serviceScope")) {
                    final ServiceScope srvScope = this.parseServiceScope(parser);
                    if (srvScope == null) {
                        continue;
                    }
                    retScope.addServiceScope(srvScope);
                }
                else {
                    this.skip(parser);
                }
            }
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "scope");
        return retScope;
    }
    
    private Programme parseProgramme(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "programme");
        Programme retProg = null;
        final String cridIdString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "id");
        final String shortCridIdString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "shortId");
        int shortId = -1;
        try {
            shortId = Integer.parseInt(shortCridIdString.trim());
        }
        catch (NumberFormatException numExc) {
            numExc.printStackTrace();
        }
        if (shortId != -1 && cridIdString != null) {
            final String versionString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "version");
            final String recommendationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "recommendation");
            final String broadcastString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "broadcast");
            String langString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "xml:lang");
            int version = 1;
            if (versionString != null) {
                try {
                    version = Integer.parseInt(versionString.trim());
                }
                catch (NumberFormatException numExc2) {
                    numExc2.printStackTrace();
                }
            }
            boolean isRecomm = false;
            if (recommendationString != null && recommendationString.trim().equals("yes")) {
                isRecomm = true;
            }
            final BroadcastType broadType = BroadcastType.BROADCAST_TYPE_ON_AIR;
            if (broadcastString != null) {
                BroadcastType.fromString(broadcastString.trim());
            }
            if (langString == null) {
                langString = this.mDocumentLanguage;
            }
            retProg = new Programme(new ShortCrid(shortId), new Crid(cridIdString.trim()), version, isRecomm, broadType, langString);
            while (parser.next() != 3) {
                if (parser.getEventType() != 2) {
                    continue;
                }
                final String tagName = parser.getName();
                if (tagName.equals("shortName")) {
                    retProg.addName(this.parseName(parser, "shortName", NameType.NAME_SHORT));
                }
                else if (tagName.equals("mediumName")) {
                    retProg.addName(this.parseName(parser, "mediumName", NameType.NAME_MEDIUM));
                }
                else if (tagName.equals("longName")) {
                    retProg.addName(this.parseName(parser, "longName", NameType.NAME_LONG));
                }
                else if (tagName.equals("mediaDescription")) {
                    retProg.addMediaDescription(this.parseMediaDescription(parser));
                }
                else if (tagName.equals("genre")) {
                    final Genre genre = this.parseGenre(parser);
                    if (genre == null) {
                        continue;
                    }
                    retProg.addGenre(genre);
                }
                else if (tagName.equals("keywords")) {
                    retProg.addKeywords(new Keywords(this.readTagText(parser)));
                }
                else if (tagName.equals("link")) {
                    final Link link = this.parseLink(parser);
                    if (link == null) {
                        continue;
                    }
                    retProg.addLink(link);
                }
                else if (tagName.equals("onDemand")) {
                    final OnDemand onDemand = this.parseOndemand(parser);
                    if (onDemand == null) {
                        continue;
                    }
                    retProg.addOnDemand(onDemand);
                }
                else if (tagName.equals("location")) {
                    retProg.addLocation(this.parseLocation(parser));
                }
                else if (tagName.equals("programmeEvent")) {
                    final ProgrammeEvent progEvent = this.parseProgrammeEvent(parser);
                    if (progEvent == null) {
                        continue;
                    }
                    retProg.addProgrammeEvent(progEvent);
                }
                else {
                    this.skip(parser);
                }
            }
        }
        else {
            parser.nextTag();
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "programme");
        return retProg;
    }
    
    private ProgrammeEvent parseProgrammeEvent(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "programmeEvent");
        ProgrammeEvent retProg = null;
        final String cridIdString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "id");
        final String shortCridIdString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "shortId");
        int shortId = -1;
        try {
            shortId = Integer.parseInt(shortCridIdString.trim());
        }
        catch (NumberFormatException numExc) {
            numExc.printStackTrace();
        }
        if (shortId != -1 && cridIdString != null) {
            final String versionString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "version");
            final String recommendationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "recommendation");
            final String broadcastString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "broadcast");
            String langString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "xml:lang");
            int version = 1;
            if (versionString != null) {
                try {
                    version = Integer.parseInt(versionString.trim());
                }
                catch (NumberFormatException numExc2) {
                    numExc2.printStackTrace();
                }
            }
            boolean isRecomm = false;
            if (recommendationString != null && recommendationString.trim().equals("yes")) {
                isRecomm = true;
            }
            final BroadcastType broadType = BroadcastType.BROADCAST_TYPE_ON_AIR;
            if (broadcastString != null) {
                BroadcastType.fromString(broadcastString.trim());
            }
            if (langString == null) {
                langString = this.mDocumentLanguage;
            }
            retProg = new ProgrammeEvent(new ShortCrid(shortId), new Crid(cridIdString.trim()), version, isRecomm, broadType, langString);
            while (parser.next() != 3) {
                if (parser.getEventType() != 2) {
                    continue;
                }
                final String tagName = parser.getName();
                if (tagName.equals("shortName")) {
                    retProg.addName(this.parseName(parser, "shortName", NameType.NAME_SHORT));
                }
                else if (tagName.equals("mediumName")) {
                    retProg.addName(this.parseName(parser, "mediumName", NameType.NAME_MEDIUM));
                }
                else if (tagName.equals("longName")) {
                    retProg.addName(this.parseName(parser, "longName", NameType.NAME_LONG));
                }
                else if (tagName.equals("mediaDescription")) {
                    retProg.addMediaDescription(this.parseMediaDescription(parser));
                }
                else if (tagName.equals("genre")) {
                    final Genre genre = this.parseGenre(parser);
                    if (genre == null) {
                        continue;
                    }
                    retProg.addGenre(genre);
                }
                else if (tagName.equals("keywords")) {
                    retProg.addKeywords(new Keywords(this.readTagText(parser)));
                }
                else if (tagName.equals("link")) {
                    final Link link = this.parseLink(parser);
                    if (link == null) {
                        continue;
                    }
                    retProg.addLink(link);
                }
                else if (tagName.equals("onDemand")) {
                    final OnDemand onDemand = this.parseOndemand(parser);
                    if (onDemand == null) {
                        continue;
                    }
                    retProg.addOnDemand(onDemand);
                }
                else if (tagName.equals("location")) {
                    retProg.addLocation(this.parseLocation(parser));
                }
                else {
                    this.skip(parser);
                }
            }
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "programmeEvent");
        return retProg;
    }
    
    private Location parseLocation(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "location");
        final Location retLoc = new Location();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("time")) {
                final Time time = this.parseTime(parser);
                if (time == null) {
                    continue;
                }
                retLoc.addTime(time);
            }
            else if (tagName.equals("relativeTime")) {
                final RelativeTime relTime = this.parseRelativeTime(parser);
                if (relTime == null) {
                    continue;
                }
                retLoc.addRelativeTime(relTime);
            }
            else if (tagName.equals("bearer")) {
                final Bearer locBearer = this.parseBearer(parser);
                if (locBearer == null) {
                    continue;
                }
                retLoc.addBearer(locBearer);
            }
            else {
                this.skip(parser);
            }
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "location");
        return retLoc;
    }
    
    private Time parseTime(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "time");
        Time retTime = null;
        final String timeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "time");
        final String durationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "duration");
        final String actualTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "actualTime");
        final String actualDurationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "actualDuration");
        if (timeString != null && durationString != null) {
            retTime = new Time(timeString.trim(), durationString.trim(), (actualTimeString != null) ? actualTimeString.trim() : null, (actualDurationString != null) ? actualDurationString.trim() : null);
        }
        parser.next();
        parser.require(3, RadioEpgPiParser.NAMESPACE, "time");
        return retTime;
    }
    
    private RelativeTime parseRelativeTime(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "relativeTime");
        RelativeTime retRelTime = null;
        final String timeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "time");
        final String durationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "duration");
        final String actualTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "actualTime");
        final String actualDurationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "actualDuration");
        if (timeString != null && durationString != null) {
            retRelTime = new RelativeTime(timeString.trim(), durationString.trim(), (actualTimeString != null) ? actualTimeString.trim() : null, (actualDurationString != null) ? actualDurationString.trim() : null);
        }
        parser.next();
        parser.require(3, RadioEpgPiParser.NAMESPACE, "relativeTime");
        return retRelTime;
    }
    
    private OnDemand parseOndemand(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "onDemand");
        OnDemand retOd = null;
        PresentationTime presTime = null;
        final List<Bearer> bearers = new ArrayList<Bearer>();
        final List<AcquisitionTime> acquTimes = new ArrayList<AcquisitionTime>();
        while (parser.next() != 3) {
            if (parser.getEventType() != 2) {
                continue;
            }
            final String tagName = parser.getName();
            if (tagName.equals("presentationTime")) {
                presTime = this.parsePresentationTime(parser);
            }
            else if (tagName.equals("bearer")) {
                final Bearer bearer = this.parseBearer(parser);
                if (bearer == null) {
                    continue;
                }
                bearers.add(bearer);
            }
            else if (tagName.equals("acquisitionTime")) {
                final AcquisitionTime acquTime = this.parseAcquisitionTime(parser);
                if (acquTime == null) {
                    continue;
                }
                acquTimes.add(acquTime);
            }
            else {
                this.skip(parser);
            }
        }
        if (presTime != null && !bearers.isEmpty()) {
            retOd = new OnDemand(presTime, bearers);
            if (!acquTimes.isEmpty()) {
                retOd.addAcquisitionTimes(acquTimes);
            }
        }
        parser.require(3, RadioEpgPiParser.NAMESPACE, "onDemand");
        return retOd;
    }
    
    private PresentationTime parsePresentationTime(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "presentationTime");
        PresentationTime retTime = null;
        final String durationString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "duration");
        final String startTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "start");
        final String endTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "end");
        if (durationString != null) {
            final Duration duration = new Duration(durationString.trim());
            TimePoint startTime = null;
            if (startTimeString != null) {
                startTime = new TimePoint(startTimeString.trim());
            }
            TimePoint endTime = null;
            if (endTimeString != null) {
                endTime = new TimePoint(endTimeString.trim());
            }
            retTime = new PresentationTime(duration);
            if (startTime != null) {
                retTime.setStartTime(startTime);
            }
            if (endTime != null) {
                retTime.setEndTime(endTime);
            }
        }
        parser.next();
        parser.require(3, RadioEpgPiParser.NAMESPACE, "presentationTime");
        return retTime;
    }
    
    private AcquisitionTime parseAcquisitionTime(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, RadioEpgPiParser.NAMESPACE, "acquisitionTime");
        AcquisitionTime acquTime = null;
        final String startTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "start");
        final String endTimeString = parser.getAttributeValue(RadioEpgPiParser.NAMESPACE, "end");
        if (startTimeString != null && endTimeString != null) {
            acquTime = new AcquisitionTime(startTimeString.trim(), endTimeString.trim());
        }
        parser.next();
        parser.require(3, RadioEpgPiParser.NAMESPACE, "acquisitionTime");
        return acquTime;
    }
}
