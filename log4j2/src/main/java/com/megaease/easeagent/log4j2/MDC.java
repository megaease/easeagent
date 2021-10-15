package com.megaease.easeagent.log4j2;

import com.megaease.easeagent.log4j2.impl.Mdc;

public class MDC {
    private static Mdc MDC = LoggerFactory.FACTORY.mdc();

    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static String get(String key) {
        return MDC.get(key);
    }
}
