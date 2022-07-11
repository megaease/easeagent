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

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.plugin.api.trace.Span;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import static org.junit.Assert.*;

public class KafkaProducerRequestTest {

    private KafkaProducerRequest createOne() {
        return new KafkaProducerRequest(new ProducerRecord<>("", ""));
    }

    @Test
    public void unwrap() {
        assertTrue(createOne().unwrap() instanceof ProducerRecord);
    }

    @Test
    public void operation() {
        assertNull(createOne().operation());
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
    public void kind() {
        assertEquals(Span.Kind.PRODUCER, createOne().kind());
    }

    @Test
    public void header() {
        KafkaProducerRequest kafkaProducerRequest = createOne();
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        kafkaProducerRequest.setHeader(headerKey, headerValue);
        assertEquals(headerValue, kafkaProducerRequest.header(headerKey));
    }

    @Test
    public void name() {
        assertEquals("send", createOne().name());
    }

    @Test
    public void cacheScope() {
        assertFalse(createOne().cacheScope());
    }

    @Test
    public void setHeader() {
        header();
    }
}
