package com.megaease.easeagent.report.metric;

public class MetricItem {
    private final String type;
    private final String content;

    public MetricItem(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
