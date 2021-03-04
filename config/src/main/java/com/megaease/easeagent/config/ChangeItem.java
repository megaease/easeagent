package com.megaease.easeagent.config;

public class ChangeItem {
    private final String name;
    private final String fullName;
    private final String oldValue;
    private final String newValue;

    public ChangeItem(String name, String fullName, String oldValue, String newValue) {
        this.name = name;
        this.fullName = fullName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
