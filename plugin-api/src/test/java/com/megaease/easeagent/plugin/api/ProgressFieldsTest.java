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

package com.megaease.easeagent.plugin.api;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.plugin.api.ProgressFields.*;
import static org.junit.Assert.*;

public class ProgressFieldsTest {

    @Test
    public void changeListener() {
        getForwardedHeaders();
    }

    @Test
    public void isProgressFields() {
        assertFalse(ProgressFields.isProgressFields(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + "abc"));
        assertTrue(ProgressFields.isProgressFields(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG));
        assertTrue(ProgressFields.isProgressFields(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG + "abc"));
        assertTrue(ProgressFields.isProgressFields(OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG + "abc"));
        assertFalse(ProgressFields.isProgressFields("c" + OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG + "abc"));
    }

    @Test
    public void isEmpty() {
        assertTrue(true);
        assertTrue(ProgressFields.isEmpty(new String[0]));
        assertFalse(ProgressFields.isEmpty(new String[1]));
    }

    @Test
    public void getForwardedHeaders() {
        String key = EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG;
        assertTrue(ProgressFields.getForwardedHeaders().isEmpty());
        ProgressFields.changeListener().accept(Collections.singletonMap(key, "a,b,c"));
        assertFalse(ProgressFields.getForwardedHeaders().isEmpty());
        assertEquals(3, ProgressFields.getForwardedHeaders().size());
        assertTrue(ProgressFields.getForwardedHeaders().contains("b"));
        ProgressFields.changeListener().accept(Collections.singletonMap(key, "a,b"));
        assertFalse(ProgressFields.getForwardedHeaders().isEmpty());
        assertTrue(ProgressFields.getForwardedHeaders().contains("a"));
        assertTrue(ProgressFields.getForwardedHeaders().contains("b"));
        assertFalse(ProgressFields.getForwardedHeaders().contains("c"));
        ProgressFields.changeListener().accept(Collections.singletonMap(key, ""));
        assertTrue(ProgressFields.getForwardedHeaders().isEmpty());
    }

    @Test
    public void getResponseHoldTagFields() {
        String keyPrefix = OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG;
        assertTrue(ProgressFields.isEmpty(ProgressFields.getResponseHoldTagFields()));
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + "aaa", "bbb"));
        assertFalse(ProgressFields.isEmpty(ProgressFields.getResponseHoldTagFields()));
        assertEquals(1, ProgressFields.getResponseHoldTagFields().length);
        assertEquals("bbb", ProgressFields.getResponseHoldTagFields()[0]);
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + "aaa", ""));
        assertTrue(ProgressFields.isEmpty(ProgressFields.getResponseHoldTagFields()));
        Map<String, String> map = new HashMap<>();
        map.put(keyPrefix + "aaa", "bbb");
        map.put(keyPrefix + "ccc", "ddd");
        map.put(keyPrefix + "ffff", "fff");
        ProgressFields.changeListener().accept(map);
        assertEquals(3, ProgressFields.getResponseHoldTagFields().length);
        map.replaceAll((s, v) -> "");
        ProgressFields.changeListener().accept(map);
        assertTrue(ProgressFields.isEmpty(ProgressFields.getResponseHoldTagFields()));
    }

    @Test
    public void getServerTags() {
        String keyPrefix = OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG;
        assertTrue(ProgressFields.getServiceTags().isEmpty());
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + "aaa", "bbb"));
        assertFalse(ProgressFields.getServiceTags().isEmpty());
        assertEquals(1, ProgressFields.getServiceTags().size());
        assertEquals("bbb", ProgressFields.getServiceTags().get("aaa"));
        ProgressFields.changeListener().accept(Collections.singletonMap(keyPrefix + "aaa", ""));
        assertTrue(ProgressFields.getServiceTags().isEmpty());
        Map<String, String> map = new HashMap<>();
        map.put(keyPrefix + "aaa", "bbb");
        map.put(keyPrefix + "ccc", "ddd");
        map.put(keyPrefix + "ffff", "fff");
        ProgressFields.changeListener().accept(map);
        assertEquals(3, ProgressFields.getServiceTags().size());
        assertEquals("bbb", ProgressFields.getServiceTags().get("aaa"));
        assertEquals("ddd", ProgressFields.getServiceTags().get("ccc"));
        assertEquals("fff", ProgressFields.getServiceTags().get("ffff"));
        map.replaceAll((s, v) -> "");
        ProgressFields.changeListener().accept(map);
        assertTrue(ProgressFields.getServiceTags().isEmpty());
    }


}
