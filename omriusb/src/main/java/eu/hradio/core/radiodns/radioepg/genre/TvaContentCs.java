package eu.hradio.core.radiodns.radioepg.genre;

import java.util.*;

public class TvaContentCs
{
    private static final HashMap<String, String> mContentCs2011;
    
    public static String getContent(final String termId) {
        final String ret = TvaContentCs.mContentCs2011.get(termId);
        if (ret != null) {
            return ret;
        }
        return "";
    }
    
    static {
        mContentCs2011 = new HashMap<String, String>() {
            {
                this.put("3.0", "Proprietary");
                this.put("3.1", "NON-FICTION/INFORMATION");
                this.put("3.1.1", "News");
                this.put("3.1.1.1", "Daily news");
                this.put("3.1.1.2", "Special news/edition");
                this.put("3.1.1.3", "Special Report");
                this.put("3.1.1.4", "Commentary");
                this.put("3.1.1.5", "Periodical/General");
                this.put("3.1.1.6", "National politics/National assembly");
                this.put("3.1.1.7", "Economy/Market/Financial/Business");
                this.put("3.1.1.8", "Foreign/International");
                this.put("3.1.1.9", "Sports");
                this.put("3.1.1.10", "Cultural");
                this.put("3.1.1.10.1", "Arts");
                this.put("3.1.1.10.2", "Entertainment");
                this.put("3.1.1.10.3", "Film");
                this.put("3.1.1.10.4", "Music");
                this.put("3.1.1.10.5", "Radio");
                this.put("3.1.1.10.6", "TV");
                this.put("3.1.1.11", "Local/Regional");
                this.put("3.1.1.12", "Traffic");
                this.put("3.1.1.13", "Weather forecasts");
                this.put("3.1.1.14", "Service information");
                this.put("3.1.1.15", "Public affairs");
                this.put("3.1.1.16", "Current affairs");
                this.put("3.1.1.17", "Consumer affairs");
                this.put("3.1.2", "Religion/Philosophies");
                this.put("3.1.2.1", "Religion");
                this.put("3.1.2.1.1", "Buddhism");
                this.put("3.1.2.1.2", "Hinduism");
                this.put("3.1.2.1.3", "Christianity");
                this.put("3.1.2.1.4", "Islam");
                this.put("3.1.2.1.5", "Judaism");
                this.put("3.1.2.1.8", "Shintoism");
                this.put("3.1.2.1.9", "Baha'i");
                this.put("3.1.2.1.10", "Confucianism");
                this.put("3.1.2.1.11", "Jainism");
                this.put("3.1.2.1.12", "Sikhism");
                this.put("3.1.2.1.13", "Taoism");
                this.put("3.1.2.1.14", "Vodun (Voodoo)");
                this.put("3.1.2.1.15", "Asatru (Nordic Paganism)");
                this.put("3.1.2.1.16", "Drudism");
                this.put("3.1.2.1.17", "Goddess worship");
                this.put("3.1.2.1.18", "Wicca");
                this.put("3.1.2.1.19", "Witchcraft");
                this.put("3.1.2.1.20", "Caodaism");
                this.put("3.1.2.1.21", "Damanhur Community");
                this.put("3.1.2.1.22", "Druse (Mowahhidoon)");
                this.put("3.1.2.1.23", "Eckankar");
                this.put("3.1.2.1.24", "Gnosticism");
                this.put("3.1.2.1.25", "Rroma (Gypsies)");
                this.put("3.1.2.1.26", "Hare Krishna and ISKCON");
                this.put("3.1.2.1.27", "Lukumi (Santeria)");
                this.put("3.1.2.1.28", "Macumba");
                this.put("3.1.2.1.29", "Native American spirituality");
                this.put("3.1.2.1.30", "New Age");
                this.put("3.1.2.1.31", "Osho");
                this.put("3.1.2.1.32", "Satanism");
                this.put("3.1.2.1.33", "Scientology");
                this.put("3.1.2.1.34", "Thelema");
                this.put("3.1.2.1.35", "Unitarian-Universalism");
                this.put("3.1.2.1.36", "The Creativity Movement");
                this.put("3.1.2.1.37", "Zoroastrianism");
                this.put("3.1.2.1.38", "Quakerism");
                this.put("3.1.2.1.39", "Rastafarianism");
                this.put("3.1.2.2", "Non-religious philosophies");
                this.put("3.1.2.2.1", "Communism");
                this.put("3.1.2.2.2", "Humanism");
                this.put("3.1.2.2.5", "Libertarianism");
                this.put("3.1.2.2.7", "Deism");
                this.put("3.1.2.2.8", "Falun Gong and Falun Dafa");
                this.put("3.1.2.2.9", "Objectivism");
                this.put("3.1.2.2.10", "Universism");
                this.put("3.1.2.2.11", "Atheism");
                this.put("3.1.2.2.12", "Agnosticism");
                this.put("3.1.3", "General non-fiction");
                this.put("3.1.3.1", "Political");
                this.put("3.1.3.1.1", "Capitalism");
                this.put("3.1.3.1.2", "Fascism");
                this.put("3.1.3.1.3", "Republicanism");
                this.put("3.1.3.1.4", "Socialism");
                this.put("3.1.3.2", "Social");
                this.put("3.1.3.2.1", "Disability Issues");
                this.put("3.1.3.3", "Economic");
                this.put("3.1.3.4", "Legal");
                this.put("3.1.3.5", "Finance");
                this.put("3.1.3.6", "Education");
                this.put("3.1.3.6.1", "Pre-School");
                this.put("3.1.3.6.2", "Primary");
                this.put("3.1.3.6.3", "Secondary");
                this.put("3.1.3.6.4", "Colleges and Universities");
                this.put("3.1.3.6.5", "Adult education");
                this.put("3.1.3.6.6", "Non-formal education");
                this.put("3.1.3.6.7", "Homework");
                this.put("3.1.3.6.8", "Reading groups");
                this.put("3.1.3.6.9", "Distance learning");
                this.put("3.1.3.6.10", "Religious schools");
                this.put("3.1.3.6.11", "Student organisations");
                this.put("3.1.3.6.12", "Testing");
                this.put("3.1.3.6.13", "Theory and methods");
                this.put("3.1.3.6.14", "Interdisciplinary studies");
                this.put("3.1.3.7", "International affairs");
                this.put("3.1.3.8", "Military/Defence");
                this.put("3.1.3.9", "Industry/Manufacturing");
                this.put("3.1.3.10", "Agriculture");
                this.put("3.1.3.11", "Construction / Civil Engineering");
                this.put("3.1.3.12", "Activities");
                this.put("3.1.4", "Arts");
                this.put("3.1.4.1", "Music");
                this.put("3.1.4.2", "Dance");
                this.put("3.1.4.3", "Theatre");
                this.put("3.1.4.4", "Opera");
                this.put("3.1.4.5", "Cinema");
                this.put("3.1.4.6", "Poetry");
                this.put("3.1.4.8", "Plastic arts");
                this.put("3.1.4.9", "Fine arts");
                this.put("3.1.4.10", "Experimental arts");
                this.put("3.1.4.11", "Architecture");
                this.put("3.1.4.12", "Showbiz");
                this.put("3.1.5", "Humanities");
                this.put("3.1.5.1", "Literature");
                this.put("3.1.5.2", "Languages");
                this.put("3.1.5.3", "History");
                this.put("3.1.5.4", "Culture/Tradition/Anthropology/Ethnic studies");
                this.put("3.1.5.5", "War/Conflict");
                this.put("3.1.5.6", "Philosophy");
                this.put("3.1.5.7", "Political Science");
                this.put("3.1.6", "Sciences");
                this.put("3.1.6.1", "Applied sciences");
                this.put("3.1.6.2", "Nature/Natural sciences");
                this.put("3.1.6.2.1", "Biology");
                this.put("3.1.6.2.2", "Geology");
                this.put("3.1.6.2.3", "Botany");
                this.put("3.1.6.2.4", "Zoology");
                this.put("3.1.6.3", "Animals/Wildlife");
                this.put("3.1.6.4", "Environment/Geography");
                this.put("3.1.6.5", "Space/Universe");
                this.put("3.1.6.6", "Physical sciences");
                this.put("3.1.6.6.1", "Physics");
                this.put("3.1.6.6.2", "Chemistry");
                this.put("3.1.6.6.3", "Mechanics");
                this.put("3.1.6.6.4", "Engineering");
                this.put("3.1.6.7", "Medicine");
                this.put("3.1.6.7.1", "Alternative Medicine");
                this.put("3.1.6.8", "Technology");
                this.put("3.1.6.9", "Physiology");
                this.put("3.1.6.10", "Psychology");
                this.put("3.1.6.11", "Social");
                this.put("3.1.6.12", "Spiritual");
                this.put("3.1.6.13", "Mathematics");
                this.put("3.1.6.14", "Archaeology");
                this.put("3.1.6.15", "Statistics");
                this.put("3.1.6.16", "Liberal Arts and Science");
                this.put("3.1.7", "Human interest");
                this.put("3.1.7.1", "Reality");
                this.put("3.1.7.2", "Society/Show business/Gossip");
                this.put("3.1.7.3", "Biography/Notable personalities");
                this.put("3.1.7.4", "Personal problems");
                this.put("3.1.7.5", "Investigative journalism");
                this.put("3.1.7.6", "Museums");
                this.put("3.1.7.7", "Religious buildings");
                this.put("3.1.7.8", "Personal stories");
                this.put("3.1.7.9", "Family life");
                this.put("3.1.7.10", "libraries");
                this.put("3.1.8", "Transport and Communications");
                this.put("3.1.8.1", "Air");
                this.put("3.1.8.2", "Land");
                this.put("3.1.8.3", "Sea");
                this.put("3.1.8.4", "Space");
                this.put("3.1.9", "Events");
                this.put("3.1.9.1", "Anniversary");
                this.put("3.1.9.2", "Fair");
                this.put("3.1.9.3", "Tradeshow");
                this.put("3.1.9.4", "Musical");
                this.put("3.1.9.5", "Exhibition");
                this.put("3.1.9.6", "Royal");
                this.put("3.1.9.7", "State");
                this.put("3.1.9.8", "International");
                this.put("3.1.9.9", "National");
                this.put("3.1.9.10", "Local/Regional");
                this.put("3.1.9.11", "Seasonal");
                this.put("3.1.9.12", "Sporting");
                this.put("3.1.9.13", "Festival");
                this.put("3.1.9.14", "Concert");
                this.put("3.1.9.15", "Funeral/Memorial");
                this.put("3.1.10", "Media");
                this.put("3.1.10.1", "Advertising");
                this.put("3.1.10.2", "Print media");
                this.put("3.1.10.3", "Television");
                this.put("3.1.10.4", "Radio");
                this.put("3.1.10.5", "New media");
                this.put("3.1.10.6", "Marketing");
                this.put("3.1.11", "Listings");
                this.put("3.2", "SPORTS");
                this.put("3.2.1", "Athletics");
                this.put("3.2.1.1", "Field");
                this.put("3.2.1.2", "Track");
                this.put("3.2.1.3", "Combined athletics");
                this.put("3.2.1.4", "Running");
                this.put("3.2.1.5", "Cross-country");
                this.put("3.2.1.6", "Triathlon");
                this.put("3.2.2", "Cycling/Bicycle");
                this.put("3.2.2.1", "Mountainbike");
                this.put("3.2.2.2", "Bicross");
                this.put("3.2.2.3", "Indoor cycling");
                this.put("3.2.2.4", "Road cycling");
                this.put("3.2.3", "Team sports");
                this.put("3.2.3.1", "Football (American)");
                this.put("3.2.3.2", "Football (Australian)");
                this.put("3.2.3.3", "Football (Gaelic)");
                this.put("3.2.3.4", "Football (Indoor)");
                this.put("3.2.3.5", "Beach soccer");
                this.put("3.2.3.6", "Bandy");
                this.put("3.2.3.7", "Baseball");
                this.put("3.2.3.8", "Basketball");
                this.put("3.2.3.9", "Cricket");
                this.put("3.2.3.10", "Croquet");
                this.put("3.2.3.11", "Faustball");
                this.put("3.2.3.12", "Football (Soccer)");
                this.put("3.2.3.13", "Handball");
                this.put("3.2.3.14", "Hockey");
                this.put("3.2.3.15", "Korfball");
                this.put("3.2.3.16", "Lacrosse");
                this.put("3.2.3.17", "Netball");
                this.put("3.2.3.18", "Roller skating");
                this.put("3.2.3.19", "Rugby");
                this.put("3.2.3.19.1", "Rugby union");
                this.put("3.2.3.19.2", "Rugby league");
                this.put("3.2.3.20", "Softball");
                this.put("3.2.3.21", "Volleyball");
                this.put("3.2.3.22", "Beach volley");
                this.put("3.2.3.23", "Hurling");
                this.put("3.2.3.24", "Flying Disc/ Frisbee");
                this.put("3.2.3.25", "Kabadi");
                this.put("3.2.3.26", "Camogie");
                this.put("3.2.3.27", "Shinty");
                this.put("3.2.3.28", "Street Soccer");
                this.put("3.2.4", "Racket sports");
                this.put("3.2.4.1", "Badminton");
                this.put("3.2.4.2", "Racketball");
                this.put("3.2.4.3", "Short tennis");
                this.put("3.2.4.4", "Soft tennis");
                this.put("3.2.4.5", "Squash");
                this.put("3.2.4.6", "Table tennis");
                this.put("3.2.4.7", "Tennis");
                this.put("3.2.5", "Martial Arts");
                this.put("3.2.5.1", "Aikido");
                this.put("3.2.5.2", "Jai-alai");
                this.put("3.2.5.3", "Judo");
                this.put("3.2.5.4", "Ju-jitsu");
                this.put("3.2.5.5", "Karate");
                this.put("3.2.5.6", "Sumo/Fighting games");
                this.put("3.2.5.7", "Sambo");
                this.put("3.2.5.8", "Taekwondo");
                this.put("3.2.6", "Water sports");
                this.put("3.2.6.1", "Bodyboarding");
                this.put("3.2.6.2", "Yatching");
                this.put("3.2.6.3", "Canoeing");
                this.put("3.2.6.4", "Diving");
                this.put("3.2.6.5", "Fishing");
                this.put("3.2.6.6", "Polo");
                this.put("3.2.6.7", "Rowing");
                this.put("3.2.6.8", "Sailing");
                this.put("3.2.6.9", "Sub-aquatics");
                this.put("3.2.6.10", "Surfing");
                this.put("3.2.6.11", "Swimming");
                this.put("3.2.6.12", "Water polo");
                this.put("3.2.6.13", "Water skiing");
                this.put("3.2.6.14", "Windsurfing");
                this.put("3.2.7", "Winter sports");
                this.put("3.2.7.1", "Bobsleigh/Tobogganing");
                this.put("3.2.7.2", "Curling");
                this.put("3.2.7.3", "Ice-hockey");
                this.put("3.2.7.4", "Ice-skating");
                this.put("3.2.7.5", "Luge");
                this.put("3.2.7.6", "Skating");
                this.put("3.2.7.7", "Skibob");
                this.put("3.2.7.8", "Skiing");
                this.put("3.2.7.9", "Sleddog");
                this.put("3.2.7.10", "Snowboarding");
                this.put("3.2.7.11", "Alpine skiing");
                this.put("3.2.7.12", "Freestyle skiing ");
                this.put("3.2.7.13", "Inline skating");
                this.put("3.2.7.14", "Nordic skiing");
                this.put("3.2.7.15", "Ski jumping");
                this.put("3.2.7.16", "Speed skating");
                this.put("3.2.7.17", "Figure skating");
                this.put("3.2.7.18", "Ice-dance");
                this.put("3.2.7.19", "Marathon");
                this.put("3.2.7.20", "Short-track");
                this.put("3.2.7.21", "Biathlon");
                this.put("3.2.8", "Motor sports");
                this.put("3.2.8.1", "Auto racing");
                this.put("3.2.8.2", "Motor boating");
                this.put("3.2.8.3", "Motor cycling");
                this.put("3.2.8.4", "Formula 1");
                this.put("3.2.8.5", "Indy car");
                this.put("3.2.8.6", "Karting");
                this.put("3.2.8.7", "Rally");
                this.put("3.2.8.8", "Trucking");
                this.put("3.2.8.9", "Tractor pulling");
                this.put("3.2.8.10", "Stock car");
                this.put("3.2.8.11", "Hill Climb");
                this.put("3.2.8.12", "Trials");
                this.put("3.2.9", "'Social' sports");
                this.put("3.2.9.1", "Billiards");
                this.put("3.2.9.2", "Boules");
                this.put("3.2.9.3", "Bowling");
                this.put("3.2.9.5", "Dance sport");
                this.put("3.2.9.6", "Darts");
                this.put("3.2.9.7", "Pool");
                this.put("3.2.9.8", "Snooker");
                this.put("3.2.9.9", "Tug-of-war");
                this.put("3.2.9.10", "Balle pelote");
                this.put("3.2.9.11", "Basque pelote");
                this.put("3.2.9.12", "Trickshot");
                this.put("3.2.10", "Gymnastics");
                this.put("3.2.10.1", "Asymmetric bars");
                this.put("3.2.10.2", "Beam");
                this.put("3.2.10.3", "Horse");
                this.put("3.2.10.4", "Mat");
                this.put("3.2.10.5", "Parallel bars");
                this.put("3.2.10.6", "Rings");
                this.put("3.2.10.7", "Trampoline");
                this.put("3.2.11", "Equestrian");
                this.put("3.2.11.1", "Cart");
                this.put("3.2.11.2", "Dressage");
                this.put("3.2.11.3", "Horse racing");
                this.put("3.2.11.4", "Polo");
                this.put("3.2.11.5", "Jumping");
                this.put("3.2.11.6", "Crossing");
                this.put("3.2.11.7", "Trotting");
                this.put("3.2.12", "Adventure sports");
                this.put("3.2.12.1", "Archery");
                this.put("3.2.12.2", "Extreme sports");
                this.put("3.2.12.3", "Mountaineering");
                this.put("3.2.12.4", "Climbing");
                this.put("3.2.12.5", "Orienteering");
                this.put("3.2.12.6", "Shooting");
                this.put("3.2.12.7", "Sport acrobatics");
                this.put("3.2.12.8", "Rafting");
                this.put("3.2.12.9", "Caving");
                this.put("3.2.12.10", "Skateboarding");
                this.put("3.2.12.11", "Treking");
                this.put("3.2.13", "Strength-based sports");
                this.put("3.2.13.1", "Body-building");
                this.put("3.2.13.2", "Boxing");
                this.put("3.2.13.3", "Combative sports");
                this.put("3.2.13.4", "Power-lifting");
                this.put("3.2.13.5", "Weight-lifting");
                this.put("3.2.13.6", "Wrestling");
                this.put("3.2.14", "Air sports");
                this.put("3.2.14.1", "Ballooning");
                this.put("3.2.14.2", "Hang gliding");
                this.put("3.2.14.3", "Sky diving");
                this.put("3.2.14.4", "Delta-plane");
                this.put("3.2.14.5", "Parachuting");
                this.put("3.2.14.6", "Kiting");
                this.put("3.2.14.7", "Aeronautics");
                this.put("3.2.14.8", "Gliding");
                this.put("3.2.14.9", "Flying");
                this.put("3.2.14.10", "Aerobatics");
                this.put("3.2.15", "Golf");
                this.put("3.2.16", "Fencing");
                this.put("3.2.17", "Dog racing");
                this.put("3.2.18", "Casting");
                this.put("3.2.19", "Maccabi");
                this.put("3.2.20", "Modern Pentathlon");
                this.put("3.2.21", "Sombo");
                this.put("3.2.22", "Mind Games");
                this.put("3.2.22.1", "Bridge");
                this.put("3.2.22.2", "Chess");
                this.put("3.2.22.3", "Poker");
                this.put("3.2.23", "Traditional Games");
                this.put("3.2.24", "Disabled Sport");
                this.put("3.2.24.1", "Physically Challenged");
                this.put("3.2.24.2", "Mentally Challenged");
                this.put("3.4", "FICTION/DRAMA");
                this.put("3.4.1", "General light drama");
                this.put("3.4.2", "Soap");
                this.put("3.4.2.1", "Soap opera");
                this.put("3.4.2.2", "Soap special");
                this.put("3.4.2.3", "Soap talk");
                this.put("3.4.3", "Romance");
                this.put("3.4.4", "Legal Melodrama");
                this.put("3.4.5", "Medical melodrama");
                this.put("3.4.6", "Action");
                this.put("3.4.6.1", "Adventure");
                this.put("3.4.6.2", "Disaster");
                this.put("3.4.6.3", "Mystery");
                this.put("3.4.6.4", "Detective/Police");
                this.put("3.4.6.5", "Historical/Epic");
                this.put("3.4.6.6", "Horror");
                this.put("3.4.6.7", "Science fiction");
                this.put("3.4.6.8", "War");
                this.put("3.4.6.9", "Western");
                this.put("3.4.6.10", "Thriller");
                this.put("3.4.6.11", "Sports");
                this.put("3.4.6.12", "Martial arts");
                this.put("3.4.6.13", "Epic");
                this.put("3.4.7", "Fantasy/Fairy tale");
                this.put("3.4.8", "Erotica");
                this.put("3.4.9", "Drama based on real events (docudrama)");
                this.put("3.4.10", "Musical");
                this.put("3.4.13", "Classical drama");
                this.put("3.4.14", "Period drama");
                this.put("3.4.15", "Contemporary drama");
                this.put("3.4.16", "Religious");
                this.put("3.4.17", "Poems/Stories");
                this.put("3.4.18", "Biography");
                this.put("3.4.19", "Psychological drama");
                this.put("3.4.20", "Political Drama");
                this.put("3.5", "AMUSEMENT/ENTERTAINMENT");
                this.put("3.5.2", "Quiz/Contest");
                this.put("3.5.2.1", "Quiz");
                this.put("3.5.2.2", "Contest");
                this.put("3.5.3", "Variety/Talent");
                this.put("3.5.3.1", "Cabaret");
                this.put("3.5.3.2", "Talent");
                this.put("3.5.4", "Surprise");
                this.put("3.5.5", "Reality");
                this.put("3.5.7", "Comedy");
                this.put("3.5.7.1", "Broken comedy");
                this.put("3.5.7.2", "Romantic comedy");
                this.put("3.5.7.3", "Sitcom");
                this.put("3.5.7.4", "Satire");
                this.put("3.5.7.5", "Candid Camera");
                this.put("3.5.7.6", "Humour");
                this.put("3.5.7.7", "Spoof");
                this.put("3.5.7.8", "Character");
                this.put("3.5.7.9", "Impressionists");
                this.put("3.5.7.10", "Stunt");
                this.put("3.5.7.11", "Music comedy");
                this.put("3.5.10", "Magic/Hypnotism");
                this.put("3.5.11", "Circus");
                this.put("3.5.12", "Dating");
                this.put("3.5.13", "Bullfighting");
                this.put("3.5.14", "Rodeo");
                this.put("3.5.16", "Chat");
                this.put("3.6", "Music");
                this.put("3.6.1", "Classical music");
                this.put("3.6.1.1", "Early");
                this.put("3.6.1.2", "Classical");
                this.put("3.6.1.3", "Romantic");
                this.put("3.6.1.4", "Contemporary");
                this.put("3.6.1.5", "Light classical");
                this.put("3.6.1.6", "Middle Ages");
                this.put("3.6.1.7", "Renaissance");
                this.put("3.6.1.8", "Baroque");
                this.put("3.6.1.9", "Opera");
                this.put("3.6.1.10", "Solo instruments");
                this.put("3.6.1.11", "Chamber");
                this.put("3.6.1.12", "Symphonic");
                this.put("3.6.1.13", "Vocal");
                this.put("3.6.1.14", "Choral");
                this.put("3.6.1.15", "Song");
                this.put("3.6.1.16", "Orchestral");
                this.put("3.6.1.17", "Organ");
                this.put("3.6.1.18", "String Quartet");
                this.put("3.6.1.19", "Experimental/Avant Garde");
                this.put("3.6.2", "Jazz");
                this.put("3.6.2.1", "New Orleans/Early jazz");
                this.put("3.6.2.2", "Big band/Swing/Dixie");
                this.put("3.6.2.3", "Blues/Soul jazz");
                this.put("3.6.2.4", "Bop/Hard bop/Bebop/Postbop");
                this.put("3.6.2.5", "Traditional/Smooth");
                this.put("3.6.2.6", "Cool");
                this.put("3.6.2.7", "Modern/Avant-garde/Free");
                this.put("3.6.2.8", "Latin and World jazz");
                this.put("3.6.2.9", "Pop jazz/Jazz funk");
                this.put("3.6.2.10", "Acid jazz/Fusion");
                this.put("3.6.3", "Background music");
                this.put("3.6.3.1", "Middle-of-the-road");
                this.put("3.6.3.2", "Easy listening");
                this.put("3.6.3.3", "Ambient");
                this.put("3.6.3.4", "Mood music");
                this.put("3.6.3.5", "Oldies");
                this.put("3.6.3.6", "Love songs");
                this.put("3.6.3.7", "Dance hall");
                this.put("3.6.3.8", "Soundtrack");
                this.put("3.6.3.9", "Trailer");
                this.put("3.6.3.10", "Showtunes");
                this.put("3.6.3.11", "TV");
                this.put("3.6.3.12", "Cabaret");
                this.put("3.6.3.13", "Instrumental");
                this.put("3.6.3.14", "Sound clip");
                this.put("3.6.3.15", "Retro");
                this.put("3.6.4", "Pop-rock");
                this.put("3.6.4.1", "Pop");
                this.put("3.6.4.2", "Chanson/Ballad");
                this.put("3.6.4.3", "Traditional rock and roll");
                this.put("3.6.4.5", "Classic/Dance/Pop-rock");
                this.put("3.6.4.6", "Folk");
                this.put("3.6.4.8", "New Age");
                this.put("3.6.4.11", "Seasonal/Holiday");
                this.put("3.6.4.12", "Japanese pop-rock");
                this.put("3.6.4.13", "Karaoke/Singing contests");
                this.put("3.6.4.14", "Rock");
                this.put("3.6.4.14.1", "AOR / Slow Rock / Soft Rock");
                this.put("3.6.4.14.2", "Metal");
                this.put("3.6.4.14.3", "Glam Rock");
                this.put("3.6.4.14.4", "Punk Rock");
                this.put("3.6.4.14.5", "Prog / Symphonic Rock");
                this.put("3.6.4.14.6", "Alternative / Indie");
                this.put("3.6.4.14.7", "Experimental / Avant Garde");
                this.put("3.6.4.14.8", "Art Rock");
                this.put("3.6.4.14.9", "Folk Rock");
                this.put("3.6.4.14.10", "Nu Punk");
                this.put("3.6.4.14.11", "Grunge");
                this.put("3.6.4.14.12", "Garage Punk/Psychedelia");
                this.put("3.6.4.14.13", "Heavy Rock");
                this.put("3.6.4.15", "New Wave");
                this.put("3.6.4.16", "Easy listening / Exotica");
                this.put("3.6.4.17", "Singer/Songwriter");
                this.put("3.6.5", "Blues/Rhythm and Blues/Soul/Gospel");
                this.put("3.6.5.1", "Blues");
                this.put("3.6.5.2", "R and B");
                this.put("3.6.5.2.1", "Hip Hop Soul");
                this.put("3.6.5.2.2", "Neo Soul");
                this.put("3.6.5.2.3", "New Jack Swing");
                this.put("3.6.5.3", "Soul");
                this.put("3.6.5.4", "Gospel");
                this.put("3.6.5.5", "Rhythm and Blues");
                this.put("3.6.5.6", "Funk");
                this.put("3.6.5.6.1", "Afro Funk");
                this.put("3.6.5.6.2", "Rare Groove");
                this.put("3.6.6", "Country and Western");
                this.put("3.6.7", "Rap/Hip Hop/Reggae");
                this.put("3.6.7.1", "Rap/Christian rap");
                this.put("3.6.7.1.1", "Gangsta Rap");
                this.put("3.6.7.2", "Hip Hop/Trip-Hop");
                this.put("3.6.7.2.1", "Dirty South Hip Hop");
                this.put("3.6.7.2.2", "East Coast Hip Hop");
                this.put("3.6.7.2.4", "UK Hip Hop");
                this.put("3.6.7.2.5", "West Coast Hip Hop");
                this.put("3.6.7.3", "Reggae");
                this.put("3.6.7.3.1", "Dancehall");
                this.put("3.6.7.3.2", "Dub");
                this.put("3.6.7.3.3", "Lovers Rock");
                this.put("3.6.7.3.4", "Raggamuffin");
                this.put("3.6.7.3.5", "Rocksteady");
                this.put("3.6.7.3.6", "Ska");
                this.put("3.6.8", "Electronic/Club/Urban/Dance");
                this.put("3.6.8.1", "Acid/Punk/Acid Punk");
                this.put("3.6.8.2", "Disco");
                this.put("3.6.8.3", "Techno/Euro-Techno/Techno-Industrial/Industrial");
                this.put("3.6.8.4", "House/Techno House");
                this.put("3.6.8.4.1", "Progressive House");
                this.put("3.6.8.4.2", "Soulful Underground");
                this.put("3.6.8.5", "Rave");
                this.put("3.6.8.6", "Jungle/Tribal");
                this.put("3.6.8.7", "Trance");
                this.put("3.6.8.11", "Drum and Bass");
                this.put("3.6.8.14", "Dance/Dance-pop");
                this.put("3.6.8.15", "Garage (1990s)");
                this.put("3.6.8.16", "UK Garage");
                this.put("3.6.8.16.1", "2 Step");
                this.put("3.6.8.16.2", "4/4 Vocal Garage");
                this.put("3.6.8.16.3", "8 Bar");
                this.put("3.6.8.16.4", "Dubstep");
                this.put("3.6.8.16.5", "Eski-Beat");
                this.put("3.6.8.16.6", "Grime");
                this.put("3.6.8.16.7", "Soulful House and Garage");
                this.put("3.6.8.16.8", "Speed Garage");
                this.put("3.6.8.16.9", "Sublow");
                this.put("3.6.8.17", "Breakbeat");
                this.put("3.6.8.18", "Broken Beat");
                this.put("3.6.8.22", "Ambient Dance");
                this.put("3.6.8.23", "Alternative Dance");
                this.put("3.6.9", "World/Traditional/Ethnic/Folk music");
                this.put("3.6.9.1", "Africa");
                this.put("3.6.9.2", "Asia");
                this.put("3.6.9.3", "Australia/Oceania");
                this.put("3.6.9.4", "Caribbean");
                this.put("3.6.9.4.1", "Calypso");
                this.put("3.6.9.4.2", "SOCA");
                this.put("3.6.9.5", "Europe");
                this.put("3.6.9.5.1", "Britain");
                this.put("3.6.9.5.2", "Ireland");
                this.put("3.6.9.6", "Latin America");
                this.put("3.6.9.7", "Middle East");
                this.put("3.6.9.8", "North America");
                this.put("3.6.9.9", "Fusion");
                this.put("3.6.9.10", "Modern");
                this.put("3.6.10", "Hit-Chart/Song Requests");
                this.put("3.6.11", "Children's songs");
                this.put("3.6.12", "Event music");
                this.put("3.6.12.1", "Wedding");
                this.put("3.6.12.2", "Sports");
                this.put("3.6.12.3", "Ceremonial/Chants");
                this.put("3.6.13", "Spoken");
                this.put("3.6.14", "Dance");
                this.put("3.6.14.1", "Ballet");
                this.put("3.6.14.2", "Tap");
                this.put("3.6.14.3", "Modern");
                this.put("3.6.14.4", "Classical");
                this.put("3.6.14.5", "Ballroom");
                this.put("3.6.15", "Religious music");
                this.put("3.6.16", "Era");
                this.put("3.6.16.1", "Medieval (before 1400)");
                this.put("3.6.16.2", "Renaissance (1400-1600)");
                this.put("3.6.16.3", "Baroque (1600-1760)");
                this.put("3.6.16.4", "Classical (1730-1820)");
                this.put("3.6.16.5", "Romantic (1815-1910");
                this.put("3.6.16.6", "20th Century");
                this.put("3.6.16.6.1", "1910s");
                this.put("3.6.16.6.2", "1920s");
                this.put("3.6.16.6.3", "1930s");
                this.put("3.6.16.6.4", "1940s");
                this.put("3.6.16.6.5", "1950s");
                this.put("3.6.16.6.6", "1960s");
                this.put("3.6.16.6.7", "1970s");
                this.put("3.6.16.6.8", "1980s");
                this.put("3.6.16.6.9", "1990s");
                this.put("3.6.16.7", "21st Century");
                this.put("3.6.16.7.1", "2000s");
                this.put("3.6.16.7.2", "2010s");
                this.put("3.6.16.7.3", "2020s");
                this.put("3.6.16.7.4", "2030s");
                this.put("3.6.16.7.5", "2040s");
                this.put("3.6.16.7.6", "2050s");
                this.put("3.6.16.7.7", "2060s");
                this.put("3.6.16.7.8", "2070s");
                this.put("3.6.16.7.9", "2080s");
                this.put("3.6.16.7.10", "2090s");
                this.put("3.6.17", "Desi");
                this.put("3.6.17.1", "Asian Underground");
                this.put("3.6.17.2", "Bhangra");
                this.put("3.6.17.3", "Bollywood");
                this.put("3.6.18", "Experimental");
                this.put("3.7", "INTERACTIVE GAMES");
                this.put("3.7.1", "CONTENT GAMES CATEGORIES");
                this.put("3.7.1.1", "Action");
                this.put("3.7.1.2", "Adventure");
                this.put("3.7.1.3", "Fighting");
                this.put("3.7.1.4", "Online");
                this.put("3.7.1.5", "Platform");
                this.put("3.7.1.6", "Puzzle");
                this.put("3.7.1.7", "RPG/ MUDs");
                this.put("3.7.1.8", "Racing");
                this.put("3.7.1.9", "Simulation");
                this.put("3.7.1.10", "Sports");
                this.put("3.7.1.11", "Strategy");
                this.put("3.7.1.12", "Wrestling");
                this.put("3.7.1.13", "Classic/Retro");
                this.put("3.7.2", "STYLE");
                this.put("3.7.2.1", "Logic based");
                this.put("3.7.2.2", "Word games");
                this.put("3.7.2.3", "Positional");
                this.put("3.7.2.4", "Board games");
                this.put("3.7.2.5", "Text environments");
                this.put("3.7.2.6", "Dynamic 2D/3D graphics");
                this.put("3.7.2.7", "Non-linear video");
                this.put("3.8", "LEISURE/HOBBY/LIFESTYLE");
                this.put("3.8.1", "General Consumer Advice");
                this.put("3.8.1.1", "Road safety");
                this.put("3.8.1.2", "Consumer advice");
                this.put("3.8.1.2.1", "Personal finance");
                this.put("3.8.1.3", "Employment Advice");
                this.put("3.8.1.4", "Self-help");
                this.put("3.8.2", "Computing/Technology");
                this.put("3.8.2.1", "Technology/Computing");
                this.put("3.8.2.2", "Computer Games");
                this.put("3.8.3", "Cookery, Food, Drink");
                this.put("3.8.3.1", "Cookery");
                this.put("3.8.3.2", "Food and Drink");
                this.put("3.8.4", "Homes/Interior/Gardening");
                this.put("3.8.4.1", "Do-it-yourself");
                this.put("3.8.4.2", "Home Improvement");
                this.put("3.8.4.3", "Gardening");
                this.put("3.8.4.4", "Property Buying and Selling");
                this.put("3.8.5", "Hobbies");
                this.put("3.8.5.1", "Fishing");
                this.put("3.8.5.2", "Pet");
                this.put("3.8.5.3", "Craft/Handicraft");
                this.put("3.8.5.4", "Art");
                this.put("3.8.5.5", "Music");
                this.put("3.8.5.6", "Board Games");
                this.put("3.8.5.7", "Card Cames");
                this.put("3.8.5.8", "Gaming");
                this.put("3.8.5.9", "Shopping");
                this.put("3.8.5.10", "Collectibles/Antiques");
                this.put("3.8.5.11", "Jewellery");
                this.put("3.8.5.12", "Aviation");
                this.put("3.8.5.13", "Trains");
                this.put("3.8.5.14", "Boating");
                this.put("3.8.5.15", "Ornithology");
                this.put("3.8.6", "Cars and Motoring");
                this.put("3.8.6.1", "Car");
                this.put("3.8.6.2", "Motorcycle");
                this.put("3.8.7", "Personal/Lifestyle/Family");
                this.put("3.8.7.1", "Fitness / Keep-fit");
                this.put("3.8.7.2", "Personal health");
                this.put("3.8.7.3", "Fashion");
                this.put("3.8.7.4", "House Keeping");
                this.put("3.8.7.5", "Parenting");
                this.put("3.8.7.6", "Beauty");
                this.put("3.8.7.7", "Sex education");
                this.put("3.8.9", "Travel/Tourism");
                this.put("3.8.9.1", "Holidays");
                this.put("3.8.9.2", "Adventure/Expeditions");
                this.put("3.8.9.3", "Outdoor pursuits");
                this.put("3.9", "ADULT");
            }
        };
    }
}