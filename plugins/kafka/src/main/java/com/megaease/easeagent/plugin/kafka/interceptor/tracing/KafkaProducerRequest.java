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
import org.apache.kafka.clients.producer.ProducerRecord;


/**
 * copy from zipkin.kafka.brave
 */
// intentionally not yet public until we add tag parsing functionality
final class KafkaProducerRequest implements MessagingRequest {
//    static final RemoteGetter<KafkaProducerRequest> GETTER =
//            new RemoteGetter<KafkaProducerRequest>() {
//                @Override
//                public Kind spanKind() {
//                    return Kind.PRODUCER;
//                }
//
//                @Override
//                public String get(KafkaProducerRequest request, String name) {
//                    return lastStringHeader(request.delegate.headers(), name);
//                }
//
//                @Override
//                public String toString() {
//                    return "Headers::lastHeader";
//                }
//            };
//
//    static final RemoteSetter<KafkaProducerRequest> SETTER =
//            new RemoteSetter<KafkaProducerRequest>() {
//                @Override
//                public Kind spanKind() {
//                    return Kind.PRODUCER;
//                }
//
//                @Override
//                public void put(KafkaProducerRequest request, String name, String value) {
//                    KafkaHeaders.replaceHeader(request.delegate.headers(), name, value);
//                }
//
//                @Override
//                public String toString() {
//                    return "Headers::replace";
//                }
//            };

    final ProducerRecord<?, ?> delegate;

    KafkaProducerRequest(ProducerRecord<?, ?> delegate) {
        if (delegate == null) throw new NullPointerException("delegate == null");
        this.delegate = delegate;
    }


    @Override
    public Object unwrap() {
        return delegate;
    }

    @Override
    public String operation() {
        return null;
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
    public Span.Kind kind() {
        return Span.Kind.PRODUCER;
    }

    @Override
    public String header(String name) {
        return KafkaHeaders.lastStringHeader(delegate.headers(), name);
    }

    @Override
    public String name() {
        return "send";
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
