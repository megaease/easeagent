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
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.AsyncCallback;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@AdviceTo(value = KafkaProducerAdvice.class, qualifier = "doSend", plugin = KafkaPlugin.class)
public class KafkaProducerDoSendInterceptor implements NonReentrantInterceptor {
    private static final String remoteServiceName = "kafka";
    private static final Object SCOPE = new Object();
    private static final Object SPAN = new Object();

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
        span.tag("kafka.broker", uri);
        span.kind(Span.Kind.PRODUCER).name("send");
        if (remoteServiceName != null) span.remoteServiceName(remoteServiceName);
        if (record.key() instanceof String && !"".equals(record.key())) {
            span.tag(KafkaTags.KAFKA_KEY_TAG, record.key().toString());
        }
        span.tag(KafkaTags.KAFKA_TOPIC_TAG, record.topic());
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


}
