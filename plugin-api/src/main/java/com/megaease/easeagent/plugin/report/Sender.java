package com.megaease.easeagent.plugin.report;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * borrow from OpenZipkin's Sender.
 */
public interface Sender extends Closeable {
    /**
     * Define an unique name for the sender
     */
    String name();

    /**
     * Initialize the sender with the configuration
     * @param config configuration with the prefix of "plugin.reporter.sender.[name]
     */
    void init(Config config);

    /**
     * Sends encoded data to a transport such as http or Kafka.
     *
     * @param encodedData encoded data, such as encoded spans.
     * @throws IllegalStateException if {@link #close() close} was called.
     */
    Callback<Void> send(byte[] encodedData);

    /**
     * If sender is available( not closed), return true, otherwise false.
     */
    boolean isAvailable();

    /**
     * when the configuration of sender changed, this method will be called
     * @param changes changed configuration KVs
     */
    void updateConfigs(Map<String, String> changes);
}
