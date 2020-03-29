package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaMediaTypeCs
{
    private static final HashMap<String, String> mMediaTypeCs2010;
    
    public static String getMediaType(final String termId) {
        final String ret = TvaMediaTypeCs.mMediaTypeCs2010.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mMediaTypeCs2010 = new HashMap<String, String>() {
            {
                this.put("7.0", "Proprietary");
                this.put("7.1", "Linear");
                this.put("7.1.1", "Audio only");
                this.put("7.1.2", "Video only");
                this.put("7.1.3", "Audio and video");
                this.put("7.1.4", "Multimedia");
                this.put("7.1.4.1", "Text");
                this.put("7.1.4.2", "Graphics");
                this.put("7.1.4.3", "Application");
                this.put("7.1.5", "Data");
                this.put("7.2", "Non Linear");
                this.put("7.2.1", "Audio only");
                this.put("7.2.2", "Video only");
                this.put("7.2.3", "Audio and video");
                this.put("7.2.4", "Multimedia");
                this.put("7.2.4.1", "Text");
                this.put("7.2.4.2", "Graphics");
                this.put("7.2.4.3", "Application");
                this.put("7.2.5", "Data");
                this.put("7.3", "AUDIO VIDEO ENHANCEMENTS");
                this.put("7.3.1", "Linear with non-sync");
                this.put("7.3.2", "Linear with sync");
                this.put("7.3.3", "Multi stream audio");
                this.put("7.3.4", "Multi stream video");
                this.put("7.3.5", "Non-linear single video/audio stream");
                this.put("7.3.6", "Non-linear multi stream");
                this.put("7.3.7", "Hybrid NVOD");
                this.put("7.3.8", "Mix and match");
                this.put("7.3.9", "Parallel 'layer controlled' audio or video support");
                this.put("7.3.10", "Linear broadcast with online insertions");
                this.put("7.3.11", "Multimedia MashUp");
            }
        };
    }
}
