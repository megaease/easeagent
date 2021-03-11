package com.megaease.easeagent.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConfigManagerMXBean {
    void updateConfigs(Map<String, String> configs);

    void updateObservability(String json) throws IOException;

    Map<String, String> getConfigs();

    List<String> availableConfigNames();

    default void healthz() {
    }
}
