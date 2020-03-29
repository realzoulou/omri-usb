package eu.hradio.httprequestwrapper.util;

import java.util.*;
import java.text.*;

public final class TimeUtils
{
    public static Date dateFromString(final String dateString) {
        final SimpleDateFormat formater = new SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        formater.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        try {
            return formater.parse(dateString);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return new Date(dateString);
        }
    }
    
    public static String dateToString(final Date date) {
        final SimpleDateFormat formater = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH);
        formater.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return formater.format(date);
    }
}
