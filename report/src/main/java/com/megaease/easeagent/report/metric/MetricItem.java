package com.megaease.easeagent.report.metric;

public class MetricItem {
    private final String key;
    private final String content;

    public MetricItem(String key, String content) {
        this.key = key;
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }
}
