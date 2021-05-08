/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.metrics.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcDataSourceMetricInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class JdbcDataSourceMetricInterceptorTest extends BaseMetricsTest {
    MetricRegistry registry;
    MethodInfo methodInfo;

    @Before
    public void before() {
        registry = new MetricRegistry();
        methodInfo = MethodInfo.builder()
                .build();
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(JdbcDataSourceMetricInterceptor.ENABLE_KEY, "false");
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor(registry, config);
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        Assert.assertTrue(registry.getMetrics().isEmpty());

    }

}
