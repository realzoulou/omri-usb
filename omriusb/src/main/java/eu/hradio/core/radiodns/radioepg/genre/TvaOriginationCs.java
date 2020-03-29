package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaOriginationCs
{
    private static final HashMap<String, String> mOriginationCs2011;
    
    public static String getOrigination(final String termId) {
        final String ret = TvaOriginationCs.mOriginationCs2011.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mOriginationCs2011 = new HashMap<String, String>() {
            {
                this.put("5.7", "Cinema");
                this.put("5.7.1", "Made on location");
                this.put("5.7.2", "Made in studio");
                this.put("5.7.3", "Made by the consumer");
                this.put("5.7.4", "Produced by major studio");
                this.put("5.7.5", "Produced by independent studio");
                this.put("5.8", "TV");
                this.put("5.8.1", "Made on location");
                this.put("5.8.1.1", "Live");
                this.put("5.8.1.2", "As Live");
                this.put("5.8.1.3", "Edited");
                this.put("5.8.2", "Made in studio");
                this.put("5.8.2.1", "Live");
                this.put("5.8.2.2", "As Live");
                this.put("5.8.2.3", "Edited");
                this.put("5.8.3", "Made by the consumer");
                this.put("5.9", "Radio");
                this.put("5.9.1", "Made on location");
                this.put("5.9.1.1", "Live");
                this.put("5.9.1.2", "As Live");
                this.put("5.9.1.3", "Edited");
                this.put("5.9.2", "Made in studio");
                this.put("5.9.2.1", "Live");
                this.put("5.9.2.2", "As Live");
                this.put("5.9.2.3", "Edited");
                this.put("5.9.3", "Made on consumer equipment (home audio)");
                this.put("5.9.3.1", "Live");
                this.put("5.9.3.2", "As Live");
                this.put("5.9.3.3", "Edited");
                this.put("5.10", "Online Distribution");
                this.put("5.10.1", "Made on location");
                this.put("5.10.1.1", "Live");
                this.put("5.10.1.2", "As Live");
                this.put("5.10.1.3", "Edited");
                this.put("5.10.2", "Made in studio");
                this.put("5.10.2.1", "Live");
                this.put("5.10.2.2", "As Live");
                this.put("5.10.2.3", "Edited");
                this.put("5.10.3", "Made on consumer equipment");
                this.put("5.10.3.1", "Live");
                this.put("5.10.3.2", "As Live");
                this.put("5.10.3.3", "Edited");
                this.put("5.11", "Offline Distribution");
            }
        };
    }
}
