package com.megaease.easeagent.plugin.api.metric.name;

import java.util.HashMap;
import java.util.Map;

public class Tags {
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";
    private final String category;
    private final String type;
    private final String keyFieldName;
    private final Map<String, String> tags;

    public Tags(String category, String type, String keyFieldName) {
        this.category = category;
        this.type = type;
        this.keyFieldName = keyFieldName;
        this.tags = new HashMap<>();
    }

    public Tags put(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
