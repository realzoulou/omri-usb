package eu.hradio.httprequestwrapper.dtos.service_search;

import java.io.*;
import eu.hradio.httprequestwrapper.annotations.json.*;

public class GenreContainer implements Serializable
{
    private static final long serialVersionUID = -6146870459381852622L;
    @JsonProperty(name = "data_source")
    private String dataSource;
    private Genre[] genres;
    
    public String getDataSource() {
        return this.dataSource;
    }
    
    public void setDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }
    
    public Genre[] getGenres() {
        return this.genres;
    }
    
    public void setGenres(final Genre[] genres) {
        this.genres = genres;
    }
}
