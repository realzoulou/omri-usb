package eu.hradio.httprequestwrapper.util;

public final class StringUtils
{
    public static String extractDigits(final String string) {
        return string.replaceAll("[^0-9]", "");
    }
    
    public static int longestSubstring(final String first, final String second) {
        if (first == null || second == null || first.length() == 0 || second.length() == 0) {
            return 0;
        }
        int maxLen = 0;
        final int fl = first.length();
        final int sl = second.length();
        final int[][] table = new int[fl][sl];
        for (int i = 0; i < fl; ++i) {
            for (int j = 0; j < sl; ++j) {
                if (first.charAt(i) == second.charAt(j)) {
                    if (i == 0 || j == 0) {
                        table[i][j] = 1;
                    }
                    else {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }
                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                    }
                }
            }
        }
        return maxLen;
    }
    
    public static boolean equals(final String string1, final String string2) {
        return string1 != null && string2 != null && string1.equals(string2);
    }
}
