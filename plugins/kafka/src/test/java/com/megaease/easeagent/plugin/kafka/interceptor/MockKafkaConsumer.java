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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Map;
import java.util.Properties;

public class MockKafkaConsumer extends KafkaConsumer<String, String> implements DynamicFieldAccessor {
    private Object data;

    public MockKafkaConsumer(Map<String, Object> configs) {
        super(configs);
    }

    public MockKafkaConsumer(Properties properties) {
        super(properties);
    }

    @Override
    public void setEaseAgent$$DynamicField$$Data(Object data) {
        this.data = data;
    }

    @Override
    public Object getEaseAgent$$DynamicField$$Data() {
        return this.data;
    }

    public static MockKafkaConsumer buildOne() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        MockKafkaConsumer kafkaConsumer = new MockKafkaConsumer(props);
        kafkaConsumer.setEaseAgent$$DynamicField$$Data(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        return kafkaConsumer;
    }
}
