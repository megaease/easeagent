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

package com.megaease.easeagent.mock.plugin.api.demo;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class InterceptorTest {
    @Test
    public void testBeforeAfter() {
        assertNull(MockEaseAgent.getLastSpan());
        final Object key = new Object();
        Interceptor interceptor = new Interceptor() {
            @Override
            public int order() {
                return Order.HIGH.getOrder();
            }

            @Override
            public void before(MethodInfo methodInfo, Context context) {
                Span span = context.nextSpan();
                span.start();
                context.put(key, span);
            }

            @Override
            public void after(MethodInfo methodInfo, Context context) {
                Span span = context.remove(key);
                span.finish();
            }
        };
        Context context = EaseAgent.getContext();
        interceptor.before(null, context);
        Span span = context.get(key);
        assertNotNull(span);
        assertNull(MockEaseAgent.getLastSpan());
        interceptor.after(null, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);
    }
}
