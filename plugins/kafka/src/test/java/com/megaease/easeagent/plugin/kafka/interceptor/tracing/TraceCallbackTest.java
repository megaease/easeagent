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

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class TraceCallbackTest {


    @Test
    public void onCompletion() throws InterruptedException {
        Context context = EaseAgent.getContext();
        Span span = context.nextSpan().start();
        TraceCallback traceCallback = new TraceCallback(span, null);
        traceCallback.onCompletion(null, null);
        ReportSpan mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        assertFalse(mockSpan.hasError());


        span = context.nextSpan().start();
        String errorInfo = "test error";
        traceCallback = new TraceCallback(span, null);
        traceCallback.onCompletion(null, new RuntimeException(errorInfo));
        mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        assertTrue(mockSpan.hasError());
        assertEquals(errorInfo, mockSpan.errorInfo());

        AtomicBoolean ran = new AtomicBoolean(false);
        span = context.nextSpan().start();
        final TraceCallback traceCallbackAsync = new TraceCallback(span, (metadata, exception) -> {
            assertTrue(EaseAgent.getContext().currentTracing().hasCurrentSpan());
            ran.set(true);
        });
        Thread thread = new Thread(() -> traceCallbackAsync.onCompletion(null, null));
        thread.start();
        thread.join();

        mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        assertFalse(mockSpan.hasError());
        assertTrue(ran.get());


    }
}
