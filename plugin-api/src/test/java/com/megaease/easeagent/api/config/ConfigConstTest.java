/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.api.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigConstTest {
    @Test
    public void testExtractHeaderName() {
        String prefix = "globalCanaryHeaders.serviceHeaders";
        assertEquals(prefix, ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS);
        assertEquals("hello", ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-aaa.0.hello"));
        assertEquals("world", ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-aaa.1.world"));
        assertNull(ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-bbb"));
        assertNull(ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + "test-bbb"));
    }
}