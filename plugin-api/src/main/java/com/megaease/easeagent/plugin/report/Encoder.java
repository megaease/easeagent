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
    EncodedData encode(T input);
}
