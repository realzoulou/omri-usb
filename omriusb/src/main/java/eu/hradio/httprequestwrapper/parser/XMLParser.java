package eu.hradio.httprequestwrapper.parser;

import java.net.*;
import eu.hradio.httprequestwrapper.exception.*;
import java.io.*;
import android.util.*;
import eu.hradio.httprequestwrapper.dtos.podcast.*;
import org.xmlpull.v1.*;
import java.util.*;

public class XMLParser
{
    private static String TAG;
    
    public Podcast parsePodcast(final String xmlUrl, final int limit) throws NetworkException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(xmlUrl).openConnection();
            connection.setReadTimeout(1000);
            connection.setConnectTimeout(1000);
            final int status = connection.getResponseCode();
            if (status != 200 && (status == 302 || status == 301 || status == 303)) {
                final String newUrl = connection.getHeaderField("Location");
                connection.disconnect();
                return this.parsePodcast(newUrl, limit);
            }
            return this.read(connection.getInputStream(), limit);
        }
        catch (IOException e) {
            throw new NetworkException(e.getCause());
        }
        catch (XmlPullParserException e2) {
            throw new NetworkException(e2.getCause());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private Podcast read(final InputStream inputStream, final int limit) throws XmlPullParserException, IOException {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            parser.setInput(inputStream, (String)null);
            parser.nextTag();
            parser.require(2, (String)null, "rss");
            parser.nextTag();
            parser.require(2, (String)null, "channel");
            parser.next();
            final Podcast podcast = this.readContainerInfo(parser);
            final List<PodcastItem> items = new ArrayList<PodcastItem>();
            int i = 0;
            while (++i < limit) {
                items.add(this.readItem(parser));
            }
            podcast.setItems(items);
            return podcast;
        }
        finally {
            inputStream.close();
        }
    }
    
    private Podcast readContainerInfo(final XmlPullParser parser) throws IOException, XmlPullParserException {
        final Podcast podcast = new Podcast();
        while (!"item".equals(parser.getName())) {
            while (parser.next() != 3) {
                if (parser.getEventType() != 2) {
                    continue;
                }
                final String name2;
                final String name = name2 = parser.getName();
                switch (name2) {
                    case "title": {
                        podcast.setTitle(this.readText(parser));
                        continue;
                    }
                    case "link": {
                        podcast.setLink(this.readText(parser));
                        continue;
                    }
                    case "description": {
                        podcast.setDescription(this.readText(parser));
                        continue;
                    }
                    case "lastBuildDate": {
                        podcast.setLastBuildDate(this.readText(parser));
                        continue;
                    }
                    case "image": {
                        podcast.setImage(this.readText(parser));
                        continue;
                    }
                    case "category": {
                        podcast.setCategory(this.readText(parser));
                        continue;
                    }
                    case "language": {
                        podcast.setLanguage(this.readText(parser));
                        continue;
                    }
                    case "generator": {
                        podcast.setGenerator(this.readText(parser));
                        continue;
                    }
                    case "itunes:author": {
                        podcast.setAuthor(this.readText(parser));
                        continue;
                    }
                    default: {
                        this.readText(parser);
                        continue;
                    }
                }
            }
        }
        return podcast;
    }
    
    private String readText(final XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == 4) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
    
    private PodcastItem readItem(final XmlPullParser parser) throws IOException, XmlPullParserException {
        final PodcastItem item = new PodcastItem();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("item")) {
                    continue;
                }
                final String name2;
                final String name = name2 = parser.getName();
                switch (name2) {
                    case "itunes:duration": {
                        item.setDuration(this.readText(parser));
                        continue;
                    }
                    case "title": {
                        item.setTitle(this.readText(parser));
                        continue;
                    }
                    case "link": {
                        item.setLink(this.readText(parser));
                        continue;
                    }
                    case "pubDate": {
                        item.setPubDate(this.readText(parser));
                        continue;
                    }
                    case "description": {
                        item.setDescription(this.readText(parser));
                        continue;
                    }
                    case "enclosure": {
                        final Enclosure enc = this.readEnclosure(parser);
                        item.setUrl(enc.url);
                        item.setMimeType(enc.type);
                        parser.nextTag();
                        continue;
                    }
                    case "itunes:image": {
                        item.setImage(parser.getAttributeValue((String)null, "href"));
                        parser.nextTag();
                        continue;
                    }
                    case "itunes:author": {
                        item.setAuthor(this.readText(parser));
                        continue;
                    }
                    default: {
                        this.readText(parser);
                        continue;
                    }
                }
            }
        }
        return item;
    }
    
    private Enclosure readEnclosure(final XmlPullParser parser) {
        final Enclosure enclosure = new Enclosure();
        try {
            enclosure.length = Long.parseLong(parser.getAttributeValue("0", "length"));
        }
        catch (NumberFormatException e) {
            enclosure.length = 0L;
        }
        enclosure.type = parser.getAttributeValue((String)null, "type");
        enclosure.url = parser.getAttributeValue((String)null, "url");
        return enclosure;
    }
    
    static {
        XMLParser.TAG = XMLParser.class.getSimpleName();
    }
    
    private static class Enclosure
    {
        long length;
        String type;
        String url;
    }
    
    interface Keys
    {
        public static final String RSS = "rss";
        public static final String HREF = "href";
        public static final String CHANNEL = "channel";
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String DESCRIPTION = "description";
        public static final String LAST_BUILD_DATE = "lastBuildDate";
        public static final String IMAGE = "image";
        public static final String CATEGORY = "category";
        public static final String LANGUAGE = "language";
        public static final String GENERATOR = "generator";
        public static final String AUTHOR = "itunes:author";
        public static final String DURATION = "itunes:duration";
        public static final String URL = "url";
        public static final String ITEM = "item";
        public static final String PUB_DATE = "pubDate";
        public static final String GUID = "guid";
        public static final String ENCLOSURE = "enclosure";
        public static final String LENGTH = "length";
        public static final String TYPE = "type";
        public static final String ITUNES_IMAGE = "itunes:image";
    }
}
