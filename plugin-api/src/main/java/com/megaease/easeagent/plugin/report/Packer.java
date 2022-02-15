/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
    EncodedData encodeList(List<EncodedData> encodedItems);

    /**
     * Calculate the size of a message package combined by a list of item
     * @param encodedItems encodes item
     * @return size of packaged message
     */
    default int messageSizeInBytes(List<EncodedData> encodedItems) {
        return packageSizeInBytes(encodedItems.stream().map(EncodedData::size).collect(Collectors.toList()));
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
