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

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.AsyncCallback;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@AdviceTo(value = KafkaProducerAdvice.class, qualifier = "doSend", plugin = KafkaPlugin.class)
public class KafkaProducerDoSendInterceptor implements NonReentrantInterceptor {
    protected static final String REMOTE_SERVICE_NAME = "kafka";
    protected static final Object SCOPE = new Object();
    protected static final Object SPAN = new Object();

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        KafkaProducer<?, ?> producer = (KafkaProducer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(producer);
        ProducerRecord<?, ?> record = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        KafkaProducerRequest request = new KafkaProducerRequest(record);
        Span span = context.producerSpan(request);
        if (span.isNoop()) {
            return;
        }
        span.tag(KafkaTags.KAFKA_BROKER_TAG, uri);
        span.kind(Span.Kind.PRODUCER).name("send");
        if (REMOTE_SERVICE_NAME != null) span.remoteServiceName(REMOTE_SERVICE_NAME);
        if (record.key() instanceof String && !"".equals(record.key())) {
            span.tag(KafkaTags.KAFKA_KEY_TAG, record.key().toString());
        }
        span.tag(KafkaTags.KAFKA_TOPIC_TAG, record.topic());
        span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.KAFKA.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.KAFKA, span, uri);
        span.start();
        context.put(SCOPE, span.maybeScope());
        context.put(SPAN, span);
        Callback tracingCallback = new TraceCallback(span, AsyncCallback.callback(methodInfo));
        methodInfo.changeArg(1, tracingCallback);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Scope scope = context.remove(SCOPE);
        Span span = context.remove(SPAN);
        if (scope == null) {
            return;
        }
        try {
            if (AsyncCallback.isAsync(AsyncCallback.callback(methodInfo))) {
                return;
            }
            if (!methodInfo.isSuccess()) {
                span.error(methodInfo.getThrowable()).finish();
            }
        } finally {
            scope.close();
        }

    }

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }
}
