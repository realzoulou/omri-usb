package eu.hradio.httprequestwrapper.dtos.programme;

import java.io.*;

public class RankedStandaloneProgramme implements Serializable
{
    private static final long serialVersionUID = -6181003252107318975L;
    private double score;
    private String source;
    private StandaloneProgramme content;
    
    public double getScore() {
        return this.score;
    }
    
    public void setScore(final double score) {
        this.score = score;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public StandaloneProgramme getProgramme() {
        return this.content;
    }
    
    public void setProgramme(final StandaloneProgramme programme) {
        this.content = programme;
    }
}
