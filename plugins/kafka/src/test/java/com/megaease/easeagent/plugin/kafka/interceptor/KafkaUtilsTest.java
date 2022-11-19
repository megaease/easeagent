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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.kafka.interceptor.tracing.KafkaConsumerRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaUtilsTest {

    @Test
    public void getUri() {
        assertEquals(TestConst.URIS, KafkaUtils.getUri(TestConst.URIS));
        List<String> uris = Arrays.asList(TestConst.URIS.split(","));
        assertEquals(TestConst.URIS, KafkaUtils.getUri(uris));
    }

    @Test
    public void clearHeaders() {
        ConsumerRecord<?, ?> record = new ConsumerRecord<>("", 0, 0, "", "");
        String headerKey = "testHeaderKeys";
        String headerValue = "testHeaderValue";
        record.headers().add(headerKey, headerValue.getBytes());
        assertEquals(1, record.headers().toArray().length);
        Context context = EaseAgent.getContext();
        Span span = context.nextSpan();
        KafkaConsumerRequest kafkaConsumerRequest = new KafkaConsumerRequest(null, record);
        context.consumerInject(span, kafkaConsumerRequest);
        assertTrue(record.headers().toArray().length > 1);
        int traceHeadersSize = record.headers().toArray().length - 1;
        Map<String, String> traceHeaders = KafkaUtils.clearHeaders(context, record);
        assertEquals(traceHeadersSize, traceHeaders.size());
        assertEquals(1, record.headers().toArray().length);
        assertEquals(headerValue, new String(record.headers().lastHeader(headerKey).value()));
        span.abandon();
    }

    @Test
    public void getTopic() {
        String topic = "testTopic";
        ProducerRecord producerRecord = new ProducerRecord(topic, "");
        assertEquals(topic, KafkaUtils.getTopic(producerRecord));
    }
}
