package eu.hradio.core.radiodns.radioepg.genre;

import java.io.*;

public class Genre implements Serializable
{
    private static final long serialVersionUID = 7289375598021779167L;
    private static final String TAG = "EpgGenre";
    private final String mGenreHref;
    private final String mGenreTypeString;
    private TvaClassificationSchemeType mTvaCs;
    private final GenreType mGenreType;
    private final String mGenre;
    
    public Genre(final String genreHref, final String genreType) {
        this.mTvaCs = TvaClassificationSchemeType.TVA_CS_UNKNOWN;
        this.mGenreHref = genreHref.trim();
        this.mGenreTypeString = genreType.trim();
        final String[] hrefSplit = this.mGenreHref.split(":");
        if (hrefSplit.length == 7) {
            this.mTvaCs = TvaClassificationSchemeType.fromSchemeString(hrefSplit[4]);
            final String s = hrefSplit[4];
            switch (s) {
                case "IntentionCS": {
                    this.mGenre = TvaIntentionCs.getIntention(hrefSplit[6]);
                    break;
                }
                case "IntentionCSFormatCS": {
                    this.mGenre = TvaFormatCs.getFormat(hrefSplit[6]);
                    break;
                }
                case "ContentCS": {
                    this.mGenre = TvaContentCs.getContent(hrefSplit[6]);
                    break;
                }
                case "OriginationCS": {
                    this.mGenre = TvaOriginationCs.getOrigination(hrefSplit[6]);
                    break;
                }
                case "IntendedAudienceCS": {
                    this.mGenre = TvaIntendedAudienceCs.getIntentedAudience(hrefSplit[6]);
                    break;
                }
                case "ContentAlertCS": {
                    this.mGenre = TvaContentAlertCs.getContentAlert(hrefSplit[6]);
                    break;
                }
                case "MediaTypeCS": {
                    this.mGenre = TvaMediaTypeCs.getMediaType(hrefSplit[6]);
                    break;
                }
                case "AtmosphereCS": {
                    this.mGenre = TvaAtmosphereCs.getAtmosphere(hrefSplit[6]);
                    break;
                }
                default: {
                    this.mGenre = "";
                    break;
                }
            }
        }
        else {
            this.mGenre = "";
        }
        for (final GenreType type : GenreType.values()) {
            if (type.getGenreType().equalsIgnoreCase(this.mGenreTypeString)) {
                this.mGenreType = type;
                return;
            }
        }
        this.mGenreType = GenreType.GENRE_TYPE_OTHER;
    }
    
    public Genre(final String genreHref) {
        this(genreHref, GenreType.GENRE_TYPE_MAIN.getGenreType());
    }
    
    public String getGenreHref() {
        return this.mGenreHref;
    }
    
    public GenreType getGenreType() {
        return this.mGenreType;
    }
    
    public TvaClassificationSchemeType getTvaCs() {
        return this.mTvaCs;
    }
    
    public String getGenre() {
        return this.mGenre;
    }
}
