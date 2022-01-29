/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.api.middleware;

import com.megaease.easeagent.plugin.api.MockSystemEnv;
import org.junit.Test;

import static com.megaease.easeagent.plugin.api.middleware.RedirectProcessor.ENV_EASEMESH_TAGS;
import static org.junit.Assert.*;

public class RedirectProcessorTest {

    @Test
    public void getServerTagsFromEnv() {
        assertNotEquals(RedirectProcessor.getServiceTagsFromEnv(), null);
        assertTrue(RedirectProcessor.getServiceTagsFromEnv().isEmpty());
        MockSystemEnv.set(ENV_EASEMESH_TAGS, "");
        assertTrue(RedirectProcessor.getServiceTagsFromEnv().isEmpty());
        MockSystemEnv.set(ENV_EASEMESH_TAGS, "{\"a\":\"b\"}");
        assertFalse(RedirectProcessor.getServiceTagsFromEnv().isEmpty());
        assertEquals("b", RedirectProcessor.getServiceTagsFromEnv().get("a"));
        MockSystemEnv.set(ENV_EASEMESH_TAGS, "aaa");
        assertTrue(RedirectProcessor.getServiceTagsFromEnv().isEmpty());
        MockSystemEnv.set(ENV_EASEMESH_TAGS, "{\"a\":null}");
        assertTrue(RedirectProcessor.getServiceTagsFromEnv().isEmpty());

        MockSystemEnv.set(ENV_EASEMESH_TAGS, "");
    }
}
