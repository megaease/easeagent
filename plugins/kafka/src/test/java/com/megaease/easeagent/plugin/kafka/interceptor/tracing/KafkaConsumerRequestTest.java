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

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.plugin.api.trace.Span;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class KafkaConsumerRequestTest {
    private KafkaConsumerRequest createOne() {
        return new KafkaConsumerRequest(null, new ConsumerRecord<>("", 0, 0, "", ""));
    }

    @Test
    public void operation() {
        assertEquals("receive", createOne().operation());
    }

    @Test
    public void channelKind() {
        assertNull(createOne().channelKind());
    }

    @Test
    public void channelName() {
        assertNull(createOne().channelName());
    }

    @Test
    public void unwrap() {
        assertTrue(createOne().unwrap() instanceof ConsumerRecord);
    }

    @Test
    public void kind() {
        assertEquals(Span.Kind.CONSUMER, createOne().kind());
    }

    @Test
    public void header() {
        Map<String, String> map = new HashMap<>();
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        map.put(headerKey, headerValue);
        KafkaConsumerRequest kafkaConsumerRequest = new KafkaConsumerRequest(map, new ConsumerRecord<>("", 0, 0, "", ""));
        assertEquals(headerValue, kafkaConsumerRequest.header(headerKey));
    }

    @Test
    public void name() {
        assertEquals("poll", createOne().name());
    }

    @Test
    public void cacheScope() {
        assertFalse(createOne().cacheScope());
    }

    @Test
    public void setHeader() {
        ConsumerRecord consumerRecord = new ConsumerRecord<>("", 0, 0, "", "");
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        KafkaConsumerRequest kafkaConsumerRequest = new KafkaConsumerRequest(null, consumerRecord);
        kafkaConsumerRequest.setHeader(headerKey, headerValue);

        assertEquals(headerValue, KafkaHeaders.lastStringHeader(consumerRecord.headers(), headerKey));
    }
}
