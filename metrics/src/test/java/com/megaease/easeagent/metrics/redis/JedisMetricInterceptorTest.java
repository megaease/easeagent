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

package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class JedisMetricInterceptorTest extends BaseMetricsTest {

    @Test
    public void invokeSuccess() {
        Config config = this.createConfig(AbstractRedisMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();

        JedisMetricInterceptor interceptor = new JedisMetricInterceptor(metricRegistry, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("get").build();
        String key = interceptor.getKey(methodInfo, context);
        Assert.assertEquals(methodInfo.getMethod(), key);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());
    }

    @Test
    public void invokeErr() {
        Config config = this.createConfig(AbstractRedisMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();

        JedisMetricInterceptor interceptor = new JedisMetricInterceptor(metricRegistry, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("get").throwable(new Exception()).build();
        String key = interceptor.getKey(methodInfo, context);
        Assert.assertEquals(methodInfo.getMethod(), key);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR)).getCount());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(AbstractRedisMetricInterceptor.ENABLE_KEY, "false");
        MetricRegistry metricRegistry = new MetricRegistry();

        JedisMetricInterceptor interceptor = new JedisMetricInterceptor(metricRegistry, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("get").build();
        String key = interceptor.getKey(methodInfo, context);
        Assert.assertEquals(methodInfo.getMethod(), key);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }
}
