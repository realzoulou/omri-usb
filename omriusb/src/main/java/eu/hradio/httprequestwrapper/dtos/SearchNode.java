package eu.hradio.httprequestwrapper.dtos;

import java.io.*;
import eu.hradio.httprequestwrapper.dtos.service_use.*;
import eu.hradio.httprequestwrapper.dtos.service_search.*;
import eu.hradio.httprequestwrapper.parser.*;
import org.json.*;
import eu.hradio.httprequestwrapper.exception.*;

public class SearchNode implements Serializable
{
    private static final long serialVersionUID = 3766553281376254378L;
    private long id;
    private String genres;
    private String[] regions;
    private String url;
    private String[] keywords;
    private Location location;
    private String hash;
    
    public static Genre[] parseGenres(final String genresStr) {
        try {
            final JSONArray jsonArray = new JSONArray(genresStr);
            final Genre[] genres = new Genre[jsonArray.length()];
            final JsonParser parser = new JsonParser();
            for (int i = 0; i < genres.length; ++i) {
                genres[i] = parser.parse(Genre.class, jsonArray.getJSONObject(i));
            }
            return genres;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public String getGenres() {
        return this.genres;
    }
    
    public void setGenres(final String genres) {
        this.genres = genres;
    }
    
    public String[] getRegions() {
        return this.regions;
    }
    
    public void setRegions(final String[] regions) {
        this.regions = regions;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(final String url) {
        this.url = url;
    }
    
    public String[] getKeywords() {
        return this.keywords;
    }
    
    public void setKeywords(final String[] keywords) {
        this.keywords = keywords;
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(final Location location) {
        this.location = location;
    }
    
    public String getHash() {
        return this.hash;
    }
    
    public void setHash(final String hash) {
        this.hash = hash;
    }
}
