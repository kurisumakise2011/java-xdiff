package x.funny.co.util;

public final class AlgoUtils {
    private AlgoUtils() {
    }

    public static int findGCP(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return i;
            }
        }
        return n;
    }

    public static int findGCS(String a, String b) {
        int n = Math.min(a.length(), b.length());
        for (int i = 1; i <= n; i++) {
            if (a.charAt(a.length() - i) != b.charAt(b.length() - i)) {
                return i - 1;
            }
        }
        return n;
    }

    public static String max(String a, String b) {
        return a.length() > b.length() ? a : b;
    }

    public static String min(String a, String b) {
        return a.length() < b.length() ? a : b;
    }
}
