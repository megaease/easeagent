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

import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import org.junit.Test;

import java.util.function.Supplier;

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
}
