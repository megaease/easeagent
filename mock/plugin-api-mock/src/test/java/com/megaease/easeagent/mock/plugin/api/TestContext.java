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

package com.megaease.easeagent.mock.plugin.api;

import com.megaease.easeagent.mock.context.ContextManagerMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

public class TestContext {
    @Test
    public void testSpan() throws InterruptedException {
        Context context = ContextManagerMock.getContext();
        assertNotNull(context);
        assertNotNull(context.currentTracing());
        assertFalse(context.isNoop());
        assertFalse(context.currentTracing().isNoop());
        final Span span = context.nextSpan();
        assertNotNull(span);
        assertFalse(span.isNoop());
        span.cacheScope();
        span.start();
        span.finish();
        Thread.sleep(10000);
    }
}
