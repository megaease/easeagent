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

package com.megaease.easeagent.context;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AsyncContextImplTest {

    @Test
    public void build() {
        try {
            AsyncContextImpl.build(null, null, null);
            assertTrue("must be throw error", false);
        } catch (Exception e) {
            assertNotNull(e);
        }
        try {
            AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, null, null);
            assertTrue("must be throw error", false);
        } catch (Exception e) {
            assertNotNull(e);
        }

        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, null);
        assertNotNull(asyncContext.getAll());
    }

    @Test
    public void isNoop() {
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, null);
        assertFalse(asyncContext.isNoop());
    }

    @Test
    public void getSpanContext() {
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, null);
        assertTrue(asyncContext.getSpanContext().isNoop());
    }

    @Test
    public void importToCurrent() {
        String name = "test_name";
        String value = "test_value";
        SessionContext sessionContext = new SessionContext();
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> sessionContext, null);
        asyncContext.put(name, value);
        assertNull(sessionContext.get(name));
        try (Cleaner cleaner = asyncContext.importToCurrent()) {
            assertEquals(value, sessionContext.get(name));
        }
        assertNull(sessionContext.get(name));

        AsyncContextImpl asyncContext2 = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> sessionContext, Collections.singletonMap(name, value));

        assertNull(sessionContext.get(name));
        try (Cleaner cleaner = asyncContext2.importToCurrent()) {
            assertEquals(value, sessionContext.get(name));
        }
        assertNull(sessionContext.get(name));

    }

    @Test
    public void getAll() {
        String name = "test_name";
        String value = "test_value";
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, Collections.singletonMap(name, value));
        assertNotNull(asyncContext.getAll());
        assertEquals(value, asyncContext.getAll().get(name));
    }

    @Test
    public void get() {
        String name = "test_name";
        String value = "test_value";
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, Collections.singletonMap(name, value));
        String v = asyncContext.get(name);
        assertNotNull(v);
        assertEquals(v, value);
        assertNull(asyncContext.get(name + "test"));

    }

    @Test
    public void put() {
        Map<Object, Object> context = new HashMap<>();
        String name = "test_name";
        String value = "test_value";
        String name2 = name + "2";
        context.put(name, value);
        AsyncContextImpl asyncContext = AsyncContextImpl.build(NoOpTracer.NO_OP_SPAN_CONTEXT, () -> null, context);
        asyncContext.put(name2, value);
        assertNull(context.get(name2));
        assertEquals(value, asyncContext.get(name));
        assertEquals(value, asyncContext.get(name2));

    }
}
