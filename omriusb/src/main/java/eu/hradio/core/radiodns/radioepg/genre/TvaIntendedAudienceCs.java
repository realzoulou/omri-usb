package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaIntendedAudienceCs
{
    private static final HashMap<String, String> mIntentedAudienceCs2011;
    
    public static String getIntentedAudience(final String termId) {
        final String ret = TvaIntendedAudienceCs.mIntentedAudienceCs2011.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mIntentedAudienceCs2011 = new HashMap<String, String>() {
            {
                this.put("4.0", "Proprietary");
                this.put("4.1", "GENERAL AUDIENCE");
                this.put("4.2", "AGE GROUPS");
                this.put("4.2.1", "Children");
                this.put("4.2.1.0", "specific single age");
                this.put("4.2.1.1", "age 4-7");
                this.put("4.2.1.2", "age 8-13");
                this.put("4.2.1.3", "age 14-15");
                this.put("4.2.1.4", "age 0-3");
                this.put("4.2.2", "Adults");
                this.put("4.2.2.1", "age 16-17");
                this.put("4.2.2.2", "age 18-24");
                this.put("4.2.2.3", "age 25-34");
                this.put("4.2.2.4", "age 35-44");
                this.put("4.2.2.5", "age 45-54");
                this.put("4.2.2.6", "age 55-64");
                this.put("4.2.2.7", "age 65+");
                this.put("4.2.2.8", "specific single age");
                this.put("4.2.3", "Teenager");
                this.put("4.2.4", "Pre-teens");
                this.put("4.3", "SOCIAL/REGIONAL/MINORITY GROUPS");
                this.put("4.3.1", "Ethnic");
                this.put("4.3.1.1", "Immigrant groups");
                this.put("4.3.1.2", "Indigenous");
                this.put("4.3.2", "Religious");
                this.put("4.3.3", "Disabled");
                this.put("4.4", "OCCUPATIONAL GROUPS");
                this.put("4.4.1", "AB");
                this.put("4.4.1.1", "A");
                this.put("4.4.1.2", "B");
                this.put("4.4.2", "C1C2");
                this.put("4.4.2.1", "White Collar worker");
                this.put("4.4.2.2", "Skilled manual labourer");
                this.put("4.4.3", "DE");
                this.put("4.4.3.1", "D");
                this.put("4.4.3.2", "E");
                this.put("4.5", "OTHER SPECIAL INTEREST/OCCUPATIONAL GROUPS");
                this.put("4.6", "GENDER");
                this.put("4.6.1", "Primarily for males");
                this.put("4.6.2", "Primarily for females");
                this.put("4.7", "GEOGRAPHICAL");
                this.put("4.7.1", "Universal");
                this.put("4.7.2", "Continental");
                this.put("4.7.3", "National");
                this.put("4.7.4", "Regional");
                this.put("4.7.5", "Local");
                this.put("4.7.6", "Multinational");
                this.put("4.8", "EDUCATION STANDARD");
                this.put("4.8.1", "Primary");
                this.put("4.8.2", "Secondary");
                this.put("4.8.3", "Tertiary");
                this.put("4.8.4", "Post Graduate/Life Long Learning");
                this.put("4.9", "LIFESTYLE STAGES");
                this.put("4.9.1", "Single");
                this.put("4.9.2", "Couple");
                this.put("4.9.3", "Family with Children 0-3");
                this.put("4.9.4", "Family with Children 4-7");
                this.put("4.9.5", "Family with Children 8-15");
                this.put("4.9.6", "Family with Children 16+");
                this.put("4.9.7", "Empty Nester");
                this.put("4.9.8", "Retired");
                this.put("4.9.9", "Family (mixed ages)");
                this.put("4.11", "LANGUAGE OF TARGET AUDIENCE");
            }
        };
    }
}
