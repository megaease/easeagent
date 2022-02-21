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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaConsumerConstructInterceptorTest {

    @Test
    public void doAfter() {
        KafkaConsumerConstructInterceptor interceptor = new KafkaConsumerConstructInterceptor();

        MockDynamicFieldAccessor mockDynamicFieldAccessor = new MockDynamicFieldAccessor();
        Map config = new HashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");


        MethodInfo methodInfo = MethodInfo.builder().invoker(mockDynamicFieldAccessor).args(new Object[]{config}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertEquals(TestConst.URIS, mockDynamicFieldAccessor.getEaseAgent$$DynamicField$$Data());

        ConsumerConfig consumerConfig = new ConsumerConfig(config);
        methodInfo = MethodInfo.builder().invoker(mockDynamicFieldAccessor).args(new Object[]{consumerConfig}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertEquals(TestConst.URIS, mockDynamicFieldAccessor.getEaseAgent$$DynamicField$$Data());


        Map emptyConfig = new HashMap();
        methodInfo = MethodInfo.builder().invoker(mockDynamicFieldAccessor).args(new Object[]{emptyConfig}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(mockDynamicFieldAccessor.getEaseAgent$$DynamicField$$Data());

    }

    @Test
    public void order() {
        KafkaConsumerConstructInterceptor interceptor = new KafkaConsumerConstructInterceptor();
        assertEquals(Order.TRACING_INIT.getOrder(), interceptor.order());

    }

    @Test
    public void getType() {
        KafkaConsumerConstructInterceptor interceptor = new KafkaConsumerConstructInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }
}
