package com.megaease.easeagent.plugin.report;

import com.megaease.easeagent.plugin.api.config.Config;

/**
 * borrow from zipkin's BytesEncoder
 * Removing Encoding enum, and add encoderName method, allow define any kind of encoder with a unique name.
 * When the name is conflict with others, it will fail when load.
 * @param <T>
 */
public interface Encoder<T> extends Packer {
    /**
     * encoder init method, called when load
     * @param config report plugin configuration
     */
    void init(Config config);

    /** The byte length of its encoded binary form */
    int sizeInBytes(T input);

    /** Serializes an object into its binary form. */
    byte[] encode(T input);
}
