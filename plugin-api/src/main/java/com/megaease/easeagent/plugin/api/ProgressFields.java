package com.megaease.easeagent.plugin.api;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ProgressFields {
    public static final String EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG = "easeagent.progress.penetration.fields";
    public static final String EASEAGENT_PROGRESS_RESPONSE_HOLD_TAG_FIELDS_CONFIG = "easeagent.progress.response.hold.tag.fields";
    /**
     * support ease mesh
     * get headers
     * X-EG-Circuit-Breaker
     * X-EG-Retryer
     * X-EG-Rate-Limiter
     * X-EG-Time-Limiter
     */
    private static final String[] EASE_MESH_HEADERS = new String[]{
        "X-EG-Circuit-Breaker",
        "X-EG-Retryer",
        "X-EG-Rate-Limiter",
        "X-EG-Time-Limiter"
    };

    private static volatile String[] TRANSPARENT_TRANSMISSION_FIELDS = new String[0];
    private static volatile String[] RESPONSE_HOLD_TAG_FIELDS = new String[0];

    public static BiFunction<String, String, String> changeListener() {
        return (key, value) -> {
            if (EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG.equals(key)) {
                setPenetrationFields(value);
            } else if (EASEAGENT_PROGRESS_RESPONSE_HOLD_TAG_FIELDS_CONFIG.equals(key)) {
                setResponseHoldTagFields(value);
            }
            return null;
        };
    }

    private static void setPenetrationFields(String fieldStr) {
        if (fieldStr == null) {
            TRANSPARENT_TRANSMISSION_FIELDS = new String[0];
            return;
        }
        List<String> list = Arrays.stream(fieldStr.split(",")).filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        for (String easeMeshHeader : EASE_MESH_HEADERS) {
            if (!list.contains(easeMeshHeader)) {
                list.add(easeMeshHeader);
            }
        }
        TRANSPARENT_TRANSMISSION_FIELDS = list.toArray(new String[0]);
    }

    private static void setResponseHoldTagFields(String fieldStr) {
        if (fieldStr == null) {
            RESPONSE_HOLD_TAG_FIELDS = new String[0];
            return;
        }
        RESPONSE_HOLD_TAG_FIELDS = Arrays.stream(fieldStr.split(",")).filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);
    }

    public static boolean isEmpty(String[] fields) {
        return fields == null || fields.length == 0;
    }

    public static String[] getPenetrationFields() {
        return TRANSPARENT_TRANSMISSION_FIELDS;
    }

    public static String[] getResponseHoldTagFields() {
        return RESPONSE_HOLD_TAG_FIELDS;
    }
}
