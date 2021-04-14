package com.megaease.easeagent.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConfigManagerMXBean {
    void updateConfigs(Map<String, String> configs);

    void updateService(String json, String version) throws IOException;

    void updateCanary(String json, String version) throws IOException;

    void updateService2(Map<String, String> configs, String version);

    void updateCanary2(Map<String, String> configs, String version);

    Map<String, String> getConfigs();

    List<String> availableConfigNames();

    default void healthz() {
    }
}
