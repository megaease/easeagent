package com.megaease.easeagent.config;

import java.util.List;
import java.util.Map;

public interface ConfigManagerMXBean {
    void updateConfigs(Map<String, String> configs);

    Map<String, String> getConfigs();

    List<String> availableConfigNames();
}
