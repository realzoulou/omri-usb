package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaAtmosphereCs
{
    private static final HashMap<String, String> mAtmosphereCs2005;
    
    public static String getAtmosphere(final String termId) {
        final String ret = TvaAtmosphereCs.mAtmosphereCs2005.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mAtmosphereCs2005 = new HashMap<String, String>() {
            {
                this.put("8.0", "Proprietary");
                this.put("8.1", "Alternative");
                this.put("8.2", "Analytical");
                this.put("8.3", "Astonishing");
                this.put("8.4", "Ambitious");
                this.put("8.5", "Black");
                this.put("8.6", "Breathtaking");
                this.put("8.7", "Chilling");
                this.put("8.8", "Coarse");
                this.put("8.9", "Compelling");
                this.put("8.10", "Confrontational");
                this.put("8.11", "Contemporary");
                this.put("8.12", "Crazy");
                this.put("8.13", "Cutting edge");
                this.put("8.14", "Eclectic");
                this.put("8.15", "Edifying");
                this.put("8.16", "Exciting");
                this.put("8.17", "Fast-moving");
                this.put("8.18", "Frantic");
                this.put("8.19", "Fun");
                this.put("8.20", "Gripping");
                this.put("8.21", "Gritty");
                this.put("8.22", "Gutsy");
                this.put("8.23", "Happy");
                this.put("8.24", "Heart-rending");
                this.put("8.25", "Heart-warming");
                this.put("8.26", "Hot");
                this.put("8.27", "Humorous");
                this.put("8.28", "Innovative");
                this.put("8.29", "Insightful");
                this.put("8.30", "Inspirational");
                this.put("8.31", "Intriguing");
                this.put("8.32", "Irreverent");
                this.put("8.33", "Laid back");
                this.put("8.34", "Outrageous");
                this.put("8.35", "Peaceful");
                this.put("8.36", "Powerful");
                this.put("8.37", "Practical");
                this.put("8.38", "Rollercoaster");
                this.put("8.39", "Romantic");
                this.put("8.40", "Rousing");
                this.put("8.41", "Sad");
                this.put("8.42", "Satirical");
                this.put("8.43", "Serious");
                this.put("8.44", "Sexy");
                this.put("8.45", "Shocking");
                this.put("8.46", "Silly");
                this.put("8.47", "Spooky");
                this.put("8.48", "Stunning");
                this.put("8.49", "Stylish");
                this.put("8.50", "Terrifying");
                this.put("8.51", "Thriller");
                this.put("8.52", "Violent");
                this.put("8.53", "Wacky");
            }
        };
    }
}
