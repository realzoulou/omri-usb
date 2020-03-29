package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaContentAlertCs
{
    private static final HashMap<String, String> mContentAlertCs2005;
    
    public static String getContentAlert(final String termId) {
        final String ret = TvaContentAlertCs.mContentAlertCs2005.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mContentAlertCs2005 = new HashMap<String, String>() {
            {
                this.put("6.0", "ALERT NOT REQUIRED");
                this.put("6.0.1", "No content that requires alerting in any of the categories below");
                this.put("6.1", "SEX");
                this.put("6.1.1", "No sex descriptors");
                this.put("6.1.2", "Obscured or implied sexual activity");
                this.put("6.1.3", "Frank portrayal of sex and sexuality");
                this.put("6.1.4", "Scenes of explicit sexual behaviour suitable for adults only");
                this.put("6.1.4.1", "One scene of explicit sexual behaviour suitable for adults only");
                this.put("6.1.4.2", "Occasional scenes of explicit sexual behaviour suitable for adults only");
                this.put("6.1.4.3", "Frequent scenes of explicit sexual behaviour suitable for adults only");
                this.put("6.1.5", "Sexual Violence");
                this.put("6.1.5.1", "One scene of sexual violence");
                this.put("6.1.5.2", "Occasional scenes of sexual violence");
                this.put("6.1.5.3", "Frequent scenes of sexual Violence");
                this.put("6.1.6", "Verbal sexual References");
                this.put("6.1.6.1", "One verbal sexual reference");
                this.put("6.1.6.2", "Occasional verbal sexual references");
                this.put("6.1.6.3", "Frequent verbal sexual references");
                this.put("6.2", "NUDITY");
                this.put("6.2.1", "No nudity descriptors");
                this.put("6.2.2", "Partial nudity");
                this.put("6.2.2.1", "One scene of partial nudity");
                this.put("6.2.2.2", "Occasional scenes of partial nudity");
                this.put("6.2.2.3", "Frequent scenes of partial nudity");
                this.put("6.2.3", "Full frontal nudity");
                this.put("6.2.3.1", "One scene of full frontal nudity");
                this.put("6.2.3.2", "Occasional scenes of full frontal nudity");
                this.put("6.2.3.3", "Frequent scenes of full frontal nudity");
                this.put("6.3", "VIOLENCE - HUMAN BEINGS");
                this.put("6.3.1", "No violence descriptors human beings");
                this.put("6.3.2", "Deliberate infliction of pain to human beings");
                this.put("6.3.2.1", "One Scene of deliberate infliction of pain to human beings");
                this.put("6.3.2.2", "Occasional deliberate infliction of pain to human beings");
                this.put("6.3.2.3", "Frequent deliberate infliction of pain to human beings");
                this.put("6.3.3", "Infliction of strong psychological or physical pain to human beings");
                this.put("6.3.3.1", "One scene of infliction of strong psychological or physical pain to human beings");
                this.put("6.3.3.2", "Occasional scenes of infliction of strong psychological or physical pain to human beings");
                this.put("6.3.3.3", "Frequent scenes of infliction of strong psychological or physical pain to human beings");
                this.put("6.3.4", "Deliberate killing of human beings");
                this.put("6.3.4.1", "One scene of deliberate killing of human beings");
                this.put("6.3.4.2", "Occasional deliberate killing of human beings");
                this.put("6.3.4.3", "Frequent deliberate killing of human beings");
                this.put("6.4", "VIOLENCE - ANIMALS");
                this.put("6.4.1", "No violence descriptors animals");
                this.put("6.4.2", "Deliberate infliction of pain to animals");
                this.put("6.4.2.1", "One scene of deliberate infliction of pain to animals");
                this.put("6.4.2.2", "Occasional deliberate infliction of pain to animals");
                this.put("6.4.2.3", "Frequent deliberate infliction of pain to animals");
                this.put("6.4.3", "Deliberate killing of animals");
                this.put("6.4.3.1", "One scene of deliberate killing of animals");
                this.put("6.4.3.2", "Occasional deliberate killing of animals");
                this.put("6.4.3.1", "Frequent deliberate killing of animals");
                this.put("6.5", "VIOLENCE - FANTASY CHARACTERS");
                this.put("6.5.1", "No violence descriptors");
                this.put("6.5.2", "Deliberate infliction of pain to fantasy characters (including animation)");
                this.put("6.5.2.1", "One scene of deliberate infliction of pain to fantasy characters (including animation)");
                this.put("6.5.2.2", "Occasional deliberate infliction of pain to fantasy characters (including animation)");
                this.put("6.5.2.3", "Frequent deliberate infliction of pain to fantasy characters (including animation)");
                this.put("6.5.3", "Deliberate killing of fantasy characters (including animation)");
                this.put("6.5.3.1", "One scene of deliberate killing of fantasy characters (including animation)");
                this.put("6.5.3.2", "Occasional deliberate killing of fantasy characters (including animation)");
                this.put("6.5.3.3", "Frequent deliberate killing of fantasy characters (including animation)");
                this.put("6.6", "LANGUAGE");
                this.put("6.6.1", "No language descriptors");
                this.put("6.6.2", "Occasional use of mild swear words and profanities");
                this.put("6.6.3", "Frequent use of mild swear words and profanities");
                this.put("6.6.4", "Occasional use of very strong language");
                this.put("6.6.5", "Frequent use of very strong language");
                this.put("6.6.6", "One use of very strong language");
                this.put("6.6.7", "Occasional use of strong language");
                this.put("6.6.8", "Frequent use of strong language");
                this.put("6.6.9", "One use of strong language");
                this.put("6.6.10", "Occasional use of offensive language (racist, homophobic, sexist)");
                this.put("6.6.11", "Frequent use of offensive language (racist, homophobic, sexist)");
                this.put("6.6.12", "One use of offensive language (racist, homophobic, sexist)");
                this.put("6.7", "DISTURBING SCENES");
                this.put("6.7.1", "No disturbing scenes descriptors");
                this.put("6.7.2", "Factual material that may cause distress, including verbal descriptions of traumatic events and the telling of sensitive human interest stories.");
                this.put("6.7.3", "Mild scenes of blood and gore (including medical procedures, injuries from accidents, terrorists attack, murder, disaster, war)");
                this.put("6.7.3.1", "One mild scene of blood and gore");
                this.put("6.7.3.2", "Occasional mild scenes of blood and gore");
                this.put("6.7.3.3", "Frequent mild scenes of blood and gore");
                this.put("6.7.4", "Severe scenes of blood and gore (as 6.7.3 above)");
                this.put("6.7.4.1", "One severe scene of blood and gore");
                this.put("6.7.4.2", "Occasional severe scenes of blood and gore (as 6.7.3 above)");
                this.put("6.7.4.3", "Frequent severe scenes of blood and gore (as 6.7.3 above)");
                this.put("6.7.5", "Scenes with extreme horror effects");
                this.put("6.7.5.1", "One scene with extreme horror effects");
                this.put("6.7.5.2", "Occasional scenes with extreme horror effects");
                this.put("6.7.5.3", "Frequent scenes with extreme horror effects");
                this.put("6.8", "DISCRIMINATION");
                this.put("6.8.1", "No discrimination descriptors");
                this.put("6.8.2", "Deliberate discrimination or the portrayal of deliberate discrimination");
                this.put("6.9", "ILLEGAL DRUGS");
                this.put("6.9.1", "No illegal drugs descriptors");
                this.put("6.9.2", "Portrayal of illegal drug use");
                this.put("6.9.2.1", "One scene of illegal drug use");
                this.put("6.9.2.2", "Occasional portrayal of illegal drug use");
                this.put("6.9.2.3", "Frequent portrayal of illegal drug use");
                this.put("6.9.3", "Portrayal of illegal drug use with instructive detail");
                this.put("6.9.3.1", "One scene of illegal drug use with instructive detail");
                this.put("6.9.3.2", "Occasional portrayal of illegal drug use with instructive detail");
                this.put("6.9.3.3", "Frequent portrayal of illegal drug use with instructive detail");
                this.put("6.10", "STROBING");
                this.put("6.10.1", "No strobing");
                this.put("6.10.2", "Strobing that could impact on those suffering from Photosensitive epilepsy");
                this.put("6.10.2.1", "One scene of strobing that could impact on those suffering from photosensitive epilepsy");
                this.put("6.10.2.2", "Occasional strobing that could impact on those suffering from photosensitive epilepsy");
                this.put("6.10.2.3", "Frequent strobing that could impact on those suffering from photosensitive epilepsy");
            }
        };
    }
}
