package com.megaease.easeagent.plugin.report;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pack a list of encoded items into a message package.
 */
public interface Packer {
    /** The encoder name */
    String name();

    /**
     * Combines a list of encoded items into an encoded list. For example, in thrift, this would be
     * length-prefixed, whereas in json, this would be comma-separated and enclosed by brackets.
     *
     * @param encodedItems encoded item
     * @return encoded list
     */
    byte[] encodeList(List<byte[]> encodedItems);

    /**
     * Calculate the size of a message package combined by a list of item
     * @param encodedItems encodes item
     * @return size of packaged message
     */
    default int messageSizeInBytes(List<byte[]> encodedItems) {
        return packageSizeInBytes(encodedItems.stream().map(v -> v.length).collect(Collectors.toList()));
    }

    /**
     * Calculate the increase size when append a new message
     * @param sizes     current message size list
     * @param newMsgSize the size of encoded message to append
     * @return the increase size of a whole message package
     */
    int appendSizeInBytes(List<Integer> sizes, int newMsgSize);

    /**
     * Calculate the whole message package size combined of items
     * @param sizes the size list of encoded items
     * @return the size of a whole message package
     */
    int packageSizeInBytes(List<Integer> sizes);
}
