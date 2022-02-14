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

package com.megaease.easeagent.plugin.redis.interceptor.metric;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.redis.RedisPlugin;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@MockEaseAgent
public class CommonRedisMetricInterceptorTest {
    protected static final String key = "test_redis_metric";
    protected static final Object START = AgentFieldReflectAccessor.getStaticFieldValue(MockCommonRedisMetricInterceptor.class, "START");

    @Test
    public void init() {
        MockCommonRedisMetricInterceptor commonRedisMetricInterceptor = new MockCommonRedisMetricInterceptor();
        RedisPlugin redisPlugin = new RedisPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(redisPlugin.getDomain(), redisPlugin.getNamespace(), commonRedisMetricInterceptor.getType());
        commonRedisMetricInterceptor.init(iPluginConfig, "", "", "");
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(MockCommonRedisMetricInterceptor.class, "REDIS_METRIC"));

    }

    @Test
    public void doBefore() {
        MockCommonRedisMetricInterceptor commonRedisMetricInterceptor = new MockCommonRedisMetricInterceptor();
        Context context = EaseAgent.getContext();
        commonRedisMetricInterceptor.doBefore(null, context);
        assertNotNull(context.get(START));
        context.remove(START);
    }

    public Map<String, Object> getMetric(LastJsonReporter lastJsonReporter) throws InterruptedException {
        List<Map<String, Object>> mapList = lastJsonReporter.waitOne(3, TimeUnit.SECONDS);
        assertNotNull(mapList);
        assertEquals(1, mapList.size());
        return mapList.get(0);
    }

    @Test
    public void doAfter() throws InterruptedException {
        MockCommonRedisMetricInterceptor commonRedisMetricInterceptor = new MockCommonRedisMetricInterceptor();
        Context context = EaseAgent.getContext();
        RedisPlugin redisPlugin = new RedisPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(redisPlugin.getDomain(), redisPlugin.getNamespace(), commonRedisMetricInterceptor.getType());
        commonRedisMetricInterceptor.init(iPluginConfig, "", "", "");

        MethodInfo methodInfo = MethodInfo.builder().invoker("tttt").method("get").build();

        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "cache-redis")
            .add("signature", "test_redis_metric");
        LastJsonReporter lastJsonReporter = ReportMock.lastMetricJsonReporter(tagVerifier::verifyAnd);

        commonRedisMetricInterceptor.doBefore(methodInfo, context);
        commonRedisMetricInterceptor.doAfter(methodInfo, context);


        Map<String, Object> metric = getMetric(lastJsonReporter);

        assertEquals(1, (int) metric.get("cnt"));
        assertEquals(0, (int) metric.get("errcnt"));

        lastJsonReporter.clean();

        methodInfo = MethodInfo.builder().invoker("tttt").method("get").throwable(new RuntimeException("test error")).build();
        commonRedisMetricInterceptor.doBefore(methodInfo, context);
        commonRedisMetricInterceptor.doAfter(methodInfo, context);

        metric = getMetric(lastJsonReporter);
        // assertEquals(2, (int) metric.get("cnt"));
        assertEquals(1, (int) metric.get("errcnt"));
    }

    @Test
    public void getType() {
        MockCommonRedisMetricInterceptor commonRedisMetricInterceptor = new MockCommonRedisMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, commonRedisMetricInterceptor.getType());
    }

    @Test
    public void getEnterKey() {
        MockCommonRedisMetricInterceptor commonRedisMetricInterceptor = new MockCommonRedisMetricInterceptor();
        Object enterKey = commonRedisMetricInterceptor.getEnterKey(null, null);
        assertSame(AgentFieldReflectAccessor.getStaticFieldValue(MockCommonRedisMetricInterceptor.class, "ENTER"), enterKey);
    }

    class MockCommonRedisMetricInterceptor extends CommonRedisMetricInterceptor {

        @Override
        public String getKey(MethodInfo methodInfo, Context context) {
            return key;
        }
    }
}
