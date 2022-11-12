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

package com.megaease.easeagent.mock.zipkin;

import brave.TracerTestUtils;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.utils.MockProvider;
import com.megaease.easeagent.zipkin.TracingProviderImpl;

public class MockTracingProvider implements MockProvider {
    private static final TracingProviderImpl TRACING_PROVIDER = new TracingProviderImpl();
    private static final Tracing TRACING;

    static {
        TRACING_PROVIDER.setConfig(MockConfig.getCONFIGS());
        TRACING_PROVIDER.setAgentReport(MockReport.getAgentReport());
        TRACING_PROVIDER.afterPropertiesSet();
        TRACING = TRACING_PROVIDER.tracing();
    }

    public static TracingProviderImpl getTracingProvider() {
        return TRACING_PROVIDER;
    }

    public static Tracing getTRACING() {
        return TRACING;
    }

    @Override
    public Object get() {
        return getTracingProvider();
    }

    public static synchronized boolean hasPendingSpans() {
        return TracerTestUtils.hashPendingSpans(TRACING.tracer());
    }

    public static synchronized void cleanPendingSpans() {
        TracerTestUtils.clean(TRACING.tracer());
    }

    public static synchronized void cleanCurrentSpan() {
        CurrentTraceContext currentContext = TRACING.currentTraceContext();
        if (currentContext instanceof ThreadLocalCurrentTraceContext) {
            ((ThreadLocalCurrentTraceContext) currentContext).clear();
        }
    }

    public static synchronized boolean hashCurrentConetxt() {
        return TRACING.currentTraceContext().get() != null;
    }
}
