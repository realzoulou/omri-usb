package eu.hradio.httprequestwrapper.util;

public final class HRadioUrlEncoder
{
    public static String encode(final String toEscape) {
        return toEscape.replace("%", "%25").replace(" ", "%20").replace("#", "%23").replace("$", "%24").replace("&", "%26").replace("@", "%40").replace("`", "%60").replace("/", "%2F").replace("(", "%28").replace(")", "%29").replace(";", "%3B").replace("<", "%3C").replace("=", "%3D").replace(">", "%3E").replace("?", "%3F").replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D").replace("^", "%5E").replace("{", "%7B").replace("|", "%7C").replace("}", "%7D").replace("~", "%7E").replace("\"", "%22").replace("+", "%2B").replace("\u2018", "%27").replace(",", "%2C");
    }
}
