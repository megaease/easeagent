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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.ProgressFields;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static com.megaease.easeagent.plugin.api.ProgressFields.EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG;
import static org.junit.Assert.*;

public class ProgressFieldsManagerTest {

    @Test
    public void init() {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put(EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG, "aaa,bbb,ccc");
        Configs configs = new Configs(source);
        ProgressFieldsManager.init(configs);
        String[] fields = ProgressFields.getPenetrationFields();
        assertFalse(ProgressFields.isEmpty(fields));
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
        source.put(EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG, "aaa,bbb,ccc");
        Configs configs = new Configs(source);
        ProgressFieldsManager.init(configs);
        String[] fields = ProgressFields.getPenetrationFields();
        assertFalse(ProgressFields.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("bbb", fields[1]);
        assertEquals("ccc", fields[2]);

        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG, "aaa,ccc"));
        fields = ProgressFields.getPenetrationFields();
        assertFalse(ProgressFields.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("ccc", fields[1]);

        configs.updateConfigs(Collections.singletonMap(EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG, "aaa,ddd,,ccc"));
        fields = ProgressFields.getPenetrationFields();
        assertFalse(ProgressFields.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("ddd", fields[1]);
        assertEquals("ccc", fields[2]);
    }
}
