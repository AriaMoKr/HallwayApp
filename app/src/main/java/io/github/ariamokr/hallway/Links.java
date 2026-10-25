package io.github.ariamokr.hallway;

import java.net.URI;
import java.util.Locale;

/** Which pages stay in the app: the hallway's origin, where every door leads. Pure, for the tests. */
final class Links {
    /** Where the app opens. */
    static final String HOME = "https://ariamokr.github.io/hallway/";
    /** The hallway's origin: every page on it opens in the app. */
    static final String ORIGIN = "https://ariamokr.github.io";
    /** What the app adds to the WebView's user agent, so the pages can tell they are in it. */
    static final String AGENT = "HallwayTV";

    private Links() {}

    /** The user agent the app's WebView sends: the system's, then " HallwayTV/<version>". */
    static String userAgent(String system, String version) {
        return system + " " + AGENT + "/" + version;
    }

    /**
     * Whether a link opens in the app: on the hallway's origin, or on {@code extra}'s (a debug build
     * started on another address, such as the dev server), else it goes to another app.
     */
    static boolean inside(String url, String extra) {
        String origin = origin(url);
        return origin != null && (origin.equals(ORIGIN) || origin.equals(origin(extra)));
    }

    /** scheme://host[:port] of an http(s) URL, lower case, or null. */
    static String origin(String url) {
        if (url == null) return null;
        try {
            URI u = new URI(url);
            String scheme = u.getScheme(), host = u.getHost();
            if (scheme == null || host == null) return null;
            scheme = scheme.toLowerCase(Locale.ROOT);
            if (!scheme.equals("https") && !scheme.equals("http")) return null;
            int port = u.getPort();
            boolean usual = port == -1 || (scheme.equals("https") && port == 443) || (scheme.equals("http") && port == 80);
            return scheme + "://" + host.toLowerCase(Locale.ROOT) + (usual ? "" : ":" + port);
        } catch (Exception e) {
            return null;
        }
    }
}
