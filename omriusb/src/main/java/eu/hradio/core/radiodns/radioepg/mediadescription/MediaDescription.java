package eu.hradio.core.radiodns.radioepg.mediadescription;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.multimedia.*;
import eu.hradio.core.radiodns.radioepg.description.*;
import java.util.*;

public class MediaDescription implements Serializable
{
    private static final long serialVersionUID = -2538642707369698324L;
    private Multimedia mMultimedia;
    private List<Description> mDescriptions;
    
    public MediaDescription() {
        this.mMultimedia = null;
        this.mDescriptions = new ArrayList<Description>();
    }
    
    public MediaDescription(final Multimedia multimedia, final Description... descriptions) {
        this.mMultimedia = null;
        this.mDescriptions = new ArrayList<Description>();
        this.mMultimedia = multimedia;
        this.mDescriptions.addAll(Arrays.asList(descriptions));
    }
    
    public void setMultimedia(final Multimedia multimedia) {
        this.mMultimedia = multimedia;
    }
    
    public void addDescription(final Description description) {
        this.mDescriptions.add(description);
    }
    
    public void addDescription(final List<Description> descriptions) {
        this.mDescriptions.addAll(descriptions);
    }
    
    public Multimedia getMultimedia() {
        return this.mMultimedia;
    }
    
    public List<Description> getDescriptions() {
        return this.mDescriptions;
    }
}
