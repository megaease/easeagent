package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.ProgressFields;

import java.util.function.BiFunction;

public class ProgressFieldsManager {
    public static void init(Configs configs) {
        BiFunction<String, String, String> changeListener = ProgressFields.changeListener();
        final String key1 = ProgressFields.EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG;
        changeListener.apply(key1, configs.getString(key1));
        final String key2 = ProgressFields.EASEAGENT_PROGRESS_RESPONSE_HOLD_TAG_FIELDS_CONFIG;
        changeListener.apply(key2, configs.getString(key2));
        configs.addChangeListener(list -> {
            for (ChangeItem changeItem : list) {
                String fullName = changeItem.getFullName();
                if (key1.equals(fullName) || key2.equals(fullName)) {
                    changeListener.apply(fullName, changeItem.getNewValue());
                }
            }
        });
    }
}
