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

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;
import brave.sampler.BoundarySampler;
import brave.sampler.CountingSampler;
import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.ITracing;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.zipkin.impl.RequestMock;
import com.megaease.easeagent.zipkin.impl.SpanImpl;
import com.megaease.easeagent.zipkin.impl.TracingImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TracingProviderImplTest {

    @Test
    public void setConfig() {
        afterPropertiesSet();
    }

    @Test
    public void setAgentReport() {
        afterPropertiesSet();

    }

    @Test
    public void afterPropertiesSet() {
        TracingProviderImpl tracingProvider = TracingProviderImplMock.TRACING_PROVIDER;
        assertNotNull(tracingProvider.tracing());
        assertNotNull(tracingProvider.tracingSupplier());
        TracingSupplier tracingSupplier = tracingProvider.tracingSupplier();
        assertNotNull(tracingSupplier.get(() -> null));
    }

    @Test
    public void tracing() {
        afterPropertiesSet();
    }

    @Test
    public void tracingSupplier() {
        afterPropertiesSet();
    }

    @Test
    public void getSampler() {
        TracingProviderImpl tracingProvider = new TracingProviderImpl();
        {
            Map<String, String> initConfigs = new HashMap<>();
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertSame(sampler, Sampler.ALWAYS_SAMPLE);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", TracingProviderImpl.SAMPLER_TYPE_COUNTING);
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertSame(sampler, Sampler.ALWAYS_SAMPLE);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", "error");
            initConfigs.put("observability.tracings.sampled", "1");
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertSame(sampler, Sampler.ALWAYS_SAMPLE);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", TracingProviderImpl.SAMPLER_TYPE_COUNTING);
            initConfigs.put("observability.tracings.sampled", "10");
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertSame(sampler, Sampler.ALWAYS_SAMPLE);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", TracingProviderImpl.SAMPLER_TYPE_COUNTING);
            initConfigs.put("observability.tracings.sampled", "0.1");
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertTrue(sampler instanceof CountingSampler);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", TracingProviderImpl.SAMPLER_TYPE_RATE_LIMITING);
            initConfigs.put("observability.tracings.sampled", "10");
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertTrue(sampler instanceof RateLimitingSampler);
        }
        {
            Map<String, String> initConfigs = new HashMap<>();
            initConfigs.put("observability.tracings.sampledType", TracingProviderImpl.SAMPLER_TYPE_BOUNDARY);
            initConfigs.put("observability.tracings.sampled", "0.0001");
            Config config = new GlobalConfigs(initConfigs);
            tracingProvider.setConfig(config);
            Sampler sampler = tracingProvider.getSampler();
            assertTrue(sampler instanceof BoundarySampler);
        }
    }

}
