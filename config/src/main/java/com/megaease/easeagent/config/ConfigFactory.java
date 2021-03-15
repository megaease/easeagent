package com.megaease.easeagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static com.megaease.easeagent.config.ValidateUtils.*;

public class ConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);

    public static Configs loadFromClasspath(ClassLoader classLoader) {
        return new Configs(new HashMap<>());
    }

    public static Configs loadFromFile(File file) throws IOException {
        Configs configs = null;
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                HashMap<String, String> map = new HashMap<>();
                for (String one : properties.stringPropertyNames()) {
                    map.put(one, properties.getProperty(one));
                }
                configs = new Configs(map);

            }
        } catch (IOException e) {
            LOGGER.warn("Load config file failure: {}", file.getAbsolutePath());
            throw e;
        }
//        validConfigs(configs);
        return configs;
    }

    private static void validConfigs(Configs configs) {
        //validate serviceName
        ValidateUtils.validate(configs, ConfigConst.SERVICE_NAME, HasText);
        //validate output
        ValidateUtils.validate(configs, ConfigConst.OUTPUT_ENABLED, HasText, Bool);
        ValidateUtils.validate(configs, ConfigConst.OUTPUT_SERVERS, HasText);
        ValidateUtils.validate(configs, ConfigConst.OUTPUT_TIMEOUT, HasText, NumberInt);
        //validate metrics
        ValidateUtils.validate(configs, ConfigConst.METRICS_ENABLED, HasText, Bool);
        //validate trace
        ValidateUtils.validate(configs, ConfigConst.TRACE_ENABLED, HasText, Bool);
        //validate trace output
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_ENABLED, HasText, Bool);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_MESSAGE_MAX_BYTES, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_MESSAGE_TIMEOUT, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_QUEUED_MAX_SIZE, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_QUEUED_MAX_SPANS, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_REPORT_THREAD, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.TRACE_OUTPUT_TOPIC, HasText);
    }
}