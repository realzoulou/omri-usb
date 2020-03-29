package eu.hradio.httprequestwrapper.dtos.podcast;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;

public class PodcastItem implements Serializable
{
    private static final long serialVersionUID = -8904803837873395901L;
    private String title;
    private String link;
    private String pubDate;
    private String description;
    private long duration;
    private String url;
    private String mimeType;
    private String image;
    private String author;
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public String getLink() {
        return this.link;
    }
    
    public void setLink(final String link) {
        this.link = link;
    }
    
    public String getPubDate() {
        return this.pubDate;
    }
    
    public void setPubDate(final String pubDate) {
        this.pubDate = pubDate;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }
    
    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getImage() {
        return this.image;
    }
    
    public void setImage(final String image) {
        this.image = image;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(final String author) {
        this.author = author;
    }
    
    public void setDuration(final String durationText) {
        final DateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        Date date;
        try {
            date = format.parse(durationText);
        }
        catch (ParseException e) {
            date = new Date(durationText);
        }
        this.duration = TimeUnit.HOURS.toMillis(date.getHours()) + TimeUnit.MINUTES.toMillis(date.getMinutes()) + TimeUnit.SECONDS.toMillis(date.getSeconds());
    }
    
    public long getDuration() {
        return this.duration;
    }
}
