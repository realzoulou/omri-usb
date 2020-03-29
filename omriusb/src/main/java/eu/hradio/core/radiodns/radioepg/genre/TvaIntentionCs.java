package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaIntentionCs
{
    private static final HashMap<String, String> mIntentionCs2005;
    
    public static String getIntention(final String termId) {
        final String ret = TvaIntentionCs.mIntentionCs2005.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mIntentionCs2005 = new HashMap<String, String>() {
            {
                this.put("1.0", "Proprietary");
                this.put("1.1", "ENTERTAIN");
                this.put("1.1.1", "Pure entertainment");
                this.put("1.1.2", "Informative Entertainment");
                this.put("1.2", "INFORM");
                this.put("1.2.1", "Government");
                this.put("1.2.2", "Pure information");
                this.put("1.2.3", "Infotainment");
                this.put("1.2.4", "Advice");
                this.put("1.3", "EDUCATE");
                this.put("1.3.1", "School Programmes");
                this.put("1.3.1.1", "Primary");
                this.put("1.3.1.2", "Secondary");
                this.put("1.3.2", "Lifelong / further education");
                this.put("1.4", "PROMOTE");
                this.put("1.5", "ADVERTISE");
                this.put("1.6", "RETAIL");
                this.put("1.6.1", "Gambling");
                this.put("1.6.2", "Home Shopping");
                this.put("1.7", "FUND RAISE/SOCIAL ACTION");
                this.put("1.7.1", "Fund Raising");
                this.put("1.7.2", "Social Action");
                this.put("1.8", "ENRICH");
                this.put("1.8.1", "General enrichment");
                this.put("1.8.2", "Inspirational enrichment");
                this.put("1.9", "EDUCATIONAL DIFFICULTY");
                this.put("1.9.1", "Very Easy");
                this.put("1.9.2", "Easy");
                this.put("1.9.3", "Medium");
                this.put("1.9.4", "Difficult");
                this.put("1.9.5", "Very Difficult");
            }
        };
    }
}
