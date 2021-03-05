package com.megaease.easeagent.report;

public class DataItem {
    private final Category category;
    private final String type;
    private final String content;

    public DataItem(Category category, String type, String content) {
        this.category = category;
        this.type = type;
        this.content = content;
    }

    public Category getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
