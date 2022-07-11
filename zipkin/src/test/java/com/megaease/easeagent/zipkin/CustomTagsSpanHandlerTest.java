/*
 * Copyright (c) 2022, MegaEase
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

package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.handler.MutableSpan;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomTagsSpanHandlerTest {
    @Test
    public void end() {
        String serviceName = "testName";
        String i = "mac";
        CustomTagsSpanHandler customTagsSpanHandler = new CustomTagsSpanHandler(() -> serviceName, i);
        MutableSpan mutableSpan = new MutableSpan();
        customTagsSpanHandler.end(null, mutableSpan, null);
        assertEquals(serviceName, mutableSpan.localServiceName());
        assertEquals(i, mutableSpan.tag(CustomTagsSpanHandler.TAG_INSTANCE));
    }

    @Test
    public void fillTags() {
        String serviceName = "testName";
        String i = "mac";
        CustomTagsSpanHandler customTagsSpanHandler = new CustomTagsSpanHandler(() -> serviceName, i);
        MutableSpan mutableSpan = new MutableSpan();
        assertTrue(mutableSpan.tags().isEmpty());
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");
        customTagsSpanHandler.fillTags(mutableSpan, tags);
        assertFalse(mutableSpan.tags().isEmpty());
        assertEquals(2, mutableSpan.tags().size());
        assertEquals("value1", mutableSpan.tag("tag1"));
        assertEquals("value2", mutableSpan.tag("tag2"));
    }
}
