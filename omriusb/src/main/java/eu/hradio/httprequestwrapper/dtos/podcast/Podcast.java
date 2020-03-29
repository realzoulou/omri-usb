package eu.hradio.httprequestwrapper.dtos.podcast;

import java.io.*;
import java.util.*;

public class Podcast implements Serializable
{
    private static final long serialVersionUID = -5872876778023873751L;
    private String title;
    private String link;
    private String description;
    private String lastBuildDate;
    private String image;
    private String category;
    private String language;
    private String generator;
    private String author;
    private List<PodcastItem> items;
    
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
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getLastBuildDate() {
        return this.lastBuildDate;
    }
    
    public void setLastBuildDate(final String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }
    
    public String getImage() {
        return this.image;
    }
    
    public void setImage(final String image) {
        this.image = image;
    }
    
    public String getCategory() {
        return this.category;
    }
    
    public void setCategory(final String category) {
        this.category = category;
    }
    
    public String getLanguage() {
        return this.language;
    }
    
    public void setLanguage(final String language) {
        this.language = language;
    }
    
    public String getGenerator() {
        return this.generator;
    }
    
    public void setGenerator(final String generator) {
        this.generator = generator;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(final String author) {
        this.author = author;
    }
    
    public List<PodcastItem> getItems() {
        return this.items;
    }
    
    public void setItems(final List<PodcastItem> items) {
        this.items = items;
    }
}
