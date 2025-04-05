package com.landmaster.novabot.util;

public class EnvVars {
    public static final String NOVABOT_TOKEN;
    public static final String NOVABOT_MEETUP_FORUM;
    public static final long NOVABOT_REACT_EXPIRY;

    static {
        NOVABOT_TOKEN = System.getenv("NOVABOT_TOKEN");
        NOVABOT_MEETUP_FORUM = System.getenv("NOVABOT_MEETUP_FORUM");
        var novabotReactExpiryRaw = System.getenv("NOVABOT_REACT_EXPIRY");
        // 7776000000 milliseconds == 90 days
        NOVABOT_REACT_EXPIRY = novabotReactExpiryRaw == null ? 7776000000L : Long.parseLong(novabotReactExpiryRaw);
    }
}
