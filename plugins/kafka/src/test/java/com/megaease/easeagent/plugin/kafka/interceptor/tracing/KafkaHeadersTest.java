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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KafkaHeadersTest {

    @Test
    public void replaceHeader() {
        ConsumerRecord<?, ?> record = new ConsumerRecord<>("", 0, 0, "", "");
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        record.headers().add(headerKey, headerValue.getBytes());
        String newHeaderValue = "testNewHeaderValue";
        assertEquals(headerValue, KafkaHeaders.lastStringHeader(record.headers(), headerKey));
        KafkaHeaders.replaceHeader(record.headers(), headerKey, newHeaderValue);
        assertEquals(newHeaderValue, KafkaHeaders.lastStringHeader(record.headers(), headerKey));
    }

    @Test
    public void lastStringHeader() {
        ConsumerRecord<?, ?> record = new ConsumerRecord<>("", 0, 0, "", "");
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        record.headers().add(headerKey, headerValue.getBytes());
        assertEquals(headerValue, KafkaHeaders.lastStringHeader(record.headers(), headerKey));
        assertNull(KafkaHeaders.lastStringHeader(record.headers(), "aaaa"));
    }
}
