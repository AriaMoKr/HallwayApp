package io.github.ariamokr.hallway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LinksTest {
    @Test
    public void theHallwaysOriginStaysInTheApp() {
        assertTrue(Links.inside(Links.HOME, Links.HOME));
        assertTrue(Links.inside("https://ariamokr.github.io/vrlbry/?from=hallway&dx=0.2", Links.HOME));
        assertTrue(Links.inside("https://AriaMoKr.github.io:443/outside/", Links.HOME));
        assertTrue(Links.inside("https://ariamokr.github.io", Links.HOME));
    }

    @Test
    public void otherSitesGoElsewhere() {
        assertFalse(Links.inside("https://www.youtube.com/watch?v=x", Links.HOME));
        assertFalse(Links.inside("http://ariamokr.github.io/hallway/", Links.HOME)); // not https
        assertFalse(Links.inside("https://ariamokr.github.io.example.com/", Links.HOME));
        assertFalse(Links.inside("https://ariamokr.github.io:8443/", Links.HOME));
        assertFalse(Links.inside("https://github.com/AriaMoKr/hallway", Links.HOME));
        assertFalse(Links.inside("mailto:someone@example.com", Links.HOME));
        assertFalse(Links.inside("intent://x#Intent;end", Links.HOME));
        assertFalse(Links.inside("not a url", Links.HOME));
        assertFalse(Links.inside(null, Links.HOME));
    }

    @Test
    public void aDebugStartAddsItsOrigin() {
        String dev = "http://127.0.0.1:8089/";
        assertTrue(Links.inside("http://127.0.0.1:8089/?from=vrlbry", dev));
        assertTrue(Links.inside(Links.HOME, dev));
        assertFalse(Links.inside("http://127.0.0.1:8088/", dev));
        assertFalse(Links.inside("http://127.0.0.1:8089/", Links.HOME));
    }

    @Test
    public void origins() {
        assertEquals("https://ariamokr.github.io", Links.origin("HTTPS://AriaMoKr.GitHub.io/hallway/"));
        assertEquals("http://127.0.0.1:8089", Links.origin("http://127.0.0.1:8089/x"));
        assertEquals("http://example.com", Links.origin("http://example.com:80/"));
        assertNull(Links.origin("file:///sdcard/x.html"));
        assertNull(Links.origin("javascript:alert(1)"));
        assertNull(Links.origin(""));
    }

    @Test
    public void theUserAgentEndsWithTheApp() {
        String system = "Mozilla/5.0 (Linux; Android 12; BRAVIA 4K VH21 Build/STT2.230206.001; wv) AppleWebKit/537.36"
                + " (KHTML, like Gecko) Version/4.0 Chrome/155.0.8059.43 Mobile Safari/537.36";
        String ua = Links.userAgent(system, "1.0.0");
        assertTrue(ua.startsWith(system));
        assertTrue(ua.endsWith(" HallwayTV/1.0.0"));
        // What the hallway looks for.
        assertTrue(ua.matches(".*\\bHallwayTV/[0-9].*"));
    }
}
