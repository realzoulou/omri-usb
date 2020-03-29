package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaFormatCs
{
    private static final HashMap<String, String> mFormatCs2011;
    
    public static String getFormat(final String termId) {
        final String ret = TvaFormatCs.mFormatCs2011.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mFormatCs2011 = new HashMap<String, String>() {
            {
                this.put("2.0", "Proprietary");
                this.put("2.1", "STRUCTURED");
                this.put("2.1.1 ", "Bulletin");
                this.put("2.1.2", "Magazine");
                this.put("2.1.2.1", "Presenter led magazine");
                this.put("2.1.2.2", "Clip led magazine");
                this.put("2.1.3", "Event");
                this.put("2.1.3.1", "Commented event");
                this.put("2.1.3.2", "Uncommented event");
                this.put("2.1.4", "Documentary");
                this.put("2.1.4.1", "Archive clips documentary");
                this.put("2.1.5", "Discussion/Interview/Debate/Talkshow");
                this.put("2.1.6", "Lecture/Speech/Presentation");
                this.put("2.1.7", "Textual (incl. relayed teletext)");
                this.put("2.1.8", "Phone-in");
                this.put("2.1.9", "DJ with discs");
                this.put("2.1.10", "Charitable Appeal");
                this.put("2.1.11", "Party-political broadcast");
                this.put("2.2", "REPRESENTATION/PLAY");
                this.put("2.2.1", "Fictional portrayal of life");
                this.put("2.2.2", "Readings");
                this.put("2.2.3", "Dramatic documentary");
                this.put("2.3", "CARTOON/ANIMATION/PUPPETRY");
                this.put("2.3.1", "Anime");
                this.put("2.3.2", "Computer");
                this.put("2.3.3", "Cartoon");
                this.put("2.3.4", "Puppetry");
                this.put("2.3.4.1", "Real time puppetry");
                this.put("2.3.4.2", "Physical model animation");
                this.put("2.4", "SHOW");
                this.put("2.4.1", "Hosted show");
                this.put("2.4.1.1", "Simple game show");
                this.put("2.4.1.2", "Big game show");
                this.put("2.4.1.3", "Telethon");
                this.put("2.4.2", "Panel-show");
                this.put("2.4.2.1", "Simple game show");
                this.put("2.4.2.2", "Big game show");
                this.put("2.4.3", "Non-hosted show");
                this.put("2.4.4", "Standup comedian(s)");
                this.put("2.4.5", "Reality Show");
                this.put("2.4.5.1", "Observational show");
                this.put("2.4.5.2", "Controlled show");
                this.put("2.4.5.3", "Makeover show");
                this.put("2.4.6", "Clips show");
                this.put("2.5", "ARTISTIC PERFORMANCE");
                this.put("2.5.1", "Solo performance");
                this.put("2.5.2", "Small ensemble performance");
                this.put("2.5.3", "Large ensemble performance");
                this.put("2.5.4", "Mixed");
                this.put("2.5.5", "Music video");
                this.put("2.6", "void");
                this.put("2.7", "INTERACTIVE");
                this.put("2.7.1", "LOCAL INTERACTIVITY");
                this.put("2.7.1.1", "Static informational");
                this.put("2.7.1.2", "Dynamic informational");
                this.put("2.7.1.3", "Viewing chats");
                this.put("2.7.1.4", "Quiz - Basic multiple choice");
                this.put("2.7.1.5", "Quiz - Text or number entry answers");
                this.put("2.7.1.6", "Re-ordering");
                this.put("2.7.1.7", "Positional");
                this.put("2.7.1.8", "Sync quiz");
                this.put("2.7.1.9", "Timer quiz");
                this.put("2.7.1.10", "Elimination and timer");
                this.put("2.7.1.11", "Categories");
                this.put("2.7.1.12", "Level based quiz/game");
                this.put("2.7.1.13", "Following a sequence");
                this.put("2.7.1.14", "Local multi player");
                this.put("2.7.1.15", "Multi stream audio-video");
                this.put("2.7.1.16", "Enhanced advertisement");
                this.put("2.7.1.17", "Logic based games");
                this.put("2.7.1.18", "Word games");
                this.put("2.7.1.19", "Positional games");
                this.put("2.7.1.20", "Board games");
                this.put("2.7.1.21", "Text based gaming");
                this.put("2.7.1.22", "Dynamic 2D/3D graphics");
                this.put("2.7.2", "INTERMITTENT RESPONSE");
                this.put("2.7.2.1", "Single impulse vote");
                this.put("2.7.2.2", "Impulse vote from choices");
                this.put("2.7.2.3", "Impulse Yes/No vote");
                this.put("2.7.2.4", "Impulse vote with a value");
                this.put("2.7.2.5", "Submit answers/form");
                this.put("2.7.2.6", "SMS using mobile");
                this.put("2.7.2.7", "SMS using TV remote");
                this.put("2.7.2.8", "Impulse gambling");
                this.put("2.7.2.9", "Impulse transaction");
                this.put("2.7.2.10", "Multi player TS networked services/games");
                this.put("2.7.2.11", "Interactive advertisement");
                this.put("2.7.3", "ALWAYS ON CONNECTION");
                this.put("2.7.3.1", "Chat Forum");
                this.put("2.7.3.2", "Chat Forum via web");
                this.put("2.7.3.3", "Threaded mail discussions");
                this.put("2.7.3.4", "Point to point");
                this.put("2.7.3.5", "3rd party point to point");
                this.put("2.7.3.6", "Voice chat using mic capability");
                this.put("2.7.3.7", "Dual player networked services/games");
                this.put("2.7.3.8", "Multi player RT networked services/games");
                this.put("2.7.3.9", "Gambling services");
                this.put("2.7.3.10", "Impulse transaction");
                this.put("2.7.3.11", "Non-linear audio-video");
            }
        };
    }
}
