package com.megaease.easeagent.plugin.api.logging;

public interface Mdc {
    void put(String key, String value);

    void remove(String key);

    String get(String key);
}
