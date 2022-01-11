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

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.Const;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaConsumerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@AdviceTo(value = KafkaConsumerAdvice.class, qualifier = "poll", plugin = KafkaPlugin.class)
public class KafkaConsumerTracingInterceptor implements NonReentrantInterceptor {
    private static final String remoteServiceName = "kafka";
    boolean singleRootSpanOnReceiveBatch = true;

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        if (!methodInfo.isSuccess() || context.currentTracing().isNoop()) {
            return;
        }
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        if (consumerRecords == null || consumerRecords.isEmpty()) {
            return;
        }
        Consumer<?, ?> consumer = (Consumer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumer);
        afterPoll(context, consumerRecords, uri);
    }

    public void afterPoll(Context context, ConsumerRecords<?, ?> records, String uri) {
        Iterator<? extends ConsumerRecord<?, ?>> iterator = records.iterator();
        Map<String, Span> consumerSpansForTopic = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            ConsumerRecord<?, ?> record = iterator.next();
            String topic = record.topic();
            Map<String, String> headers = KafkaUtils.clearHeaders(context, record);
            MessagingRequest request = new KafkaConsumerRequest(headers, record);
            if (headers == null && singleRootSpanOnReceiveBatch) {
                Span span = consumerSpansForTopic.get(topic);
                if (span == null) {
                    span = context.consumerSpan(request);
                    if (!span.isNoop()) {
                        setConsumerSpan(topic, uri, span);
                        // incur timestamp overhead only once
                        span.start();
                    }
                    consumerSpansForTopic.put(topic, span);
                }
                context.consumerInject(span, request);
            } else {
                Span span = context.consumerSpan(request);
                if (!span.isNoop()) {
                    setConsumerSpan(topic, uri, span);
                    // incur timestamp overhead only once
                    span.start().finish(); // span won't be shared by other records
                    context.consumerInject(span, request);
                }
            }
        }
        for (Span span : consumerSpansForTopic.values()) span.finish();
    }

    void setConsumerSpan(String topic, String uri, Span span) {
        span.tag(KafkaTags.KAFKA_TOPIC_TAG, topic);
        span.tag(KafkaTags.KAFKA_BROKER_TAG, uri);
        span.tag(Const.TYPE_TAG_NAME, Type.KAFKA.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.KAFKA, span, uri);
        if (remoteServiceName != null) span.remoteServiceName(remoteServiceName);
    }


}
