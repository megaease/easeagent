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

package com.megaease.easeagent.zipkin;

import brave.handler.MutableSpan;
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

public class TracingProviderImplMock {
    public static final TracingProviderImpl TRACING_PROVIDER = new TracingProviderImpl();

    static {
        TRACING_PROVIDER.setConfig(ConfigMock.getCONFIGS());
        TRACING_PROVIDER.setAgentReport(ReportMock.getAgentReport());
        TRACING_PROVIDER.afterPropertiesSet();
    }


    public static MutableSpan getMutableSpan(Span span) {
        return AgentFieldReflectAccessor.getFieldValue(span.unwrap(), "state");
    }
}
