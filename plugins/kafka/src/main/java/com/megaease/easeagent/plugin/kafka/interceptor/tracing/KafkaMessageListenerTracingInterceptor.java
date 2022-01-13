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

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaMessageListenerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

@AdviceTo(value = KafkaMessageListenerAdvice.class, plugin = KafkaPlugin.class)
public class KafkaMessageListenerTracingInterceptor implements NonReentrantInterceptor {
    private static final Object SPAN = new Object();

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumerRecord);
        Map<String, String> headers = KafkaUtils.clearHeaders(context, consumerRecord);
        MessagingRequest request = new KafkaConsumerRequest(headers, consumerRecord);
        Span span = context.consumerSpan(request).name("on-message")
            .kind(Span.Kind.CLIENT)
            .remoteServiceName("kafka")
            .tag(KafkaTags.KAFKA_BROKER_TAG, uri)
            .cacheScope()
            .start();
        span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.KAFKA.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.KAFKA, span, uri);
        context.put(SPAN, span);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Span span = context.remove(SPAN);
        if (span == null) {
            return;
        }
        try {
            if (!methodInfo.isSuccess()) {
                span.error(methodInfo.getThrowable());
            }
        } finally {
            span.finish();
        }
    }
}
