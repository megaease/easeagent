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

package com.megaease.easeagent.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.ProgressFields;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import static com.megaease.easeagent.plugin.api.ProgressFields.EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgressFieldsManagerTest {

    @Test
    public void init() {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".0", "aaa");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".1", "bbb");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".2", "ccc");
        Configs configs = new Configs(source);
        ProgressFieldsManager.init(configs);
        Set<String> fields = ProgressFields.getForwardedHeaders();
        assertFalse(fields.isEmpty());
    }

    @Test
    public void isEmpty() {
        assertTrue(ProgressFields.isEmpty(null));
        assertTrue(ProgressFields.isEmpty(new String[0]));
        assertFalse(ProgressFields.isEmpty(new String[1]));
    }

    @Test
    public void getFields() {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".0", "aaa");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".1", "bbb");
        source.put(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".2", "ccc");
        Configs configs = new Configs(source);
        ProgressFieldsManager.init(configs);
        Set<String> fields = ProgressFields.getForwardedHeaders();
        assertFalse(fields.isEmpty());
        assertTrue(fields.contains("aaa"));
        assertTrue(fields.contains("bbb"));
        assertTrue(fields.contains("ccc"));

        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG +".0", "aaa"));
        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG +".2", "ccc"));
        fields = ProgressFields.getForwardedHeaders();
        assertFalse(fields.isEmpty());
        assertTrue(fields.contains("aaa"));
        assertTrue(fields.contains("bbb"));
        assertTrue(fields.contains("ccc"));

        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG +".0", "aaa"));
        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG +".1", "ddd"));
        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG +".2", "ccc"));
        fields = ProgressFields.getForwardedHeaders();
        assertFalse(fields.isEmpty());
        assertTrue(fields.contains("aaa"));
        assertTrue(fields.contains("ddd"));
        assertTrue(fields.contains("ccc"));
        assertFalse(fields.contains("bbb"));

    }
}
