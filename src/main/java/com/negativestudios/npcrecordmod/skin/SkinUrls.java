package com.negativestudios.npcrecordmod.skin;

import java.net.URI;
import java.net.URISyntaxException;

public final class SkinUrls {
    private SkinUrls() {
    }

    public static boolean isAllowed(String value) {
        if (value == null || value.length() > 2048) return false;
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getUserInfo() == null;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
