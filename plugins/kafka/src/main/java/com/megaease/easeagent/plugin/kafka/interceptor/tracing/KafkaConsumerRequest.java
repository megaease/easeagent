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

import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.Span;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaConsumerRequest implements MessagingRequest {
    private final Map<String, String> headers;
    private final ConsumerRecord<?, ?> delegate;

    public KafkaConsumerRequest(Map<String, String> headers, ConsumerRecord<?, ?> delegate) {
        this.headers = headers;
        this.delegate = delegate;
    }

    @Override
    public String operation() {
        return "receive";
    }

    @Override
    public String channelKind() {
        return null;
    }

    @Override
    public String channelName() {
        return null;
    }

    @Override
    public Object unwrap() {
        return delegate;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.CONSUMER;
    }

    @Override
    public String header(String name) {
        return headers == null ? null : headers.get(name);
    }

    @Override
    public String name() {
        return "poll";
    }

    @Override
    public boolean cacheScope() {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {
        KafkaHeaders.replaceHeader(delegate.headers(), name, value);
    }
}
