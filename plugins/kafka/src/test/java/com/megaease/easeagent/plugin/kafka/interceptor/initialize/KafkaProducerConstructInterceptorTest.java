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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.MockKafkaProducer;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class KafkaProducerConstructInterceptorTest {

    @Test
    public void doAfter() {
        KafkaProducerConstructInterceptor interceptor = new KafkaProducerConstructInterceptor();

        Map config = new HashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        MockKafkaProducer mockKafkaProducer = new MockKafkaProducer(config);


        MethodInfo methodInfo = MethodInfo.builder().invoker(mockKafkaProducer).args(new Object[]{config}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertEquals(TestConst.URIS, mockKafkaProducer.getEaseAgent$$DynamicField$$Data());

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        mockKafkaProducer = new MockKafkaProducer(props);

        methodInfo = MethodInfo.builder().invoker(mockKafkaProducer).args(new Object[]{props}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertEquals(TestConst.URIS, mockKafkaProducer.getEaseAgent$$DynamicField$$Data());

    }

    @Test(expected = KafkaException.class)
    public void buildFail() {
        Map emptyConfig = new HashMap();
        emptyConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        emptyConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        new KafkaProducer<String, String>(emptyConfig);
    }

    @Test
    public void order() {
        KafkaProducerConstructInterceptor interceptor = new KafkaProducerConstructInterceptor();
        assertEquals(Order.TRACING_INIT.getOrder(), interceptor.order());
    }

    @Test
    public void getType() {
        KafkaProducerConstructInterceptor interceptor = new KafkaProducerConstructInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

}
