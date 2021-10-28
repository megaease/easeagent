package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransparentTransmission {
    protected static final String EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS = "easeagent.transparent.transmission.fields";

    private static volatile String[] fields = new String[0];

    public static void init(Configs configs) {
        setFields(configs.getString(EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS));
        configs.addChangeListener(list -> {
            for (ChangeItem changeItem : list) {
                if (EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS.equals(changeItem.getFullName())) {
                    setFields(changeItem.getNewValue());
                }
            }
        });
    }

    private static void setFields(String fieldStr) {
        if (fieldStr == null) {
            fields = new String[0];
            return;
        }
        fields = Arrays.stream(fieldStr.split(",")).filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);
    }


    public static boolean isEmpty(String[] fields) {
        return fields == null || fields.length == 0;
    }

    public static String[] getFields() {
        return fields;
    }
}
