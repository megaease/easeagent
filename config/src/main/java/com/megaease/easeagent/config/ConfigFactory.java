package com.megaease.easeagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class ConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);

    public static Configs loadFromClasspath(ClassLoader classLoader) {
        return new Configs(new HashMap<>());
    }

    public static Configs loadFromFile(File file) {
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                HashMap<String, String> map = new HashMap<>();
                for (String one : properties.stringPropertyNames()) {
                    map.put(one, properties.getProperty(one));
                }
                return new Configs(map);
            }
        } catch (IOException e) {
            LOGGER.warn("Load config file failure: {}", file.getAbsolutePath());
            return new Configs(Collections.emptyMap());
        }
    }
}
