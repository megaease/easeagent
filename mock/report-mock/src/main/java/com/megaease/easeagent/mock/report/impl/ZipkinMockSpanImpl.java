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

package com.megaease.easeagent.mock.report.impl;

import com.megaease.easeagent.mock.report.MockSpan;
import zipkin2.Annotation;
import zipkin2.Span;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ZipkinMockSpanImpl implements MockSpan {
    private static final Map<Span.Kind, com.megaease.easeagent.plugin.api.trace.Span.Kind> KINDS;

    static {
        Map<Span.Kind, com.megaease.easeagent.plugin.api.trace.Span.Kind> kinds = new EnumMap<>(Span.Kind.class);
        kinds.put(Span.Kind.CLIENT, com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT);
        kinds.put(Span.Kind.SERVER, com.megaease.easeagent.plugin.api.trace.Span.Kind.SERVER);
        kinds.put(Span.Kind.PRODUCER, com.megaease.easeagent.plugin.api.trace.Span.Kind.PRODUCER);
        kinds.put(Span.Kind.CONSUMER, com.megaease.easeagent.plugin.api.trace.Span.Kind.CONSUMER);
        KINDS = Collections.unmodifiableMap(kinds);
    }


    private final Span span;

    public ZipkinMockSpanImpl(@Nonnull Span span) {
        this.span = span;
    }

    @Override
    public com.megaease.easeagent.plugin.api.trace.Span.Kind kine() {
        return KINDS.get(span.kind());
    }

    @Override
    public String traceId() {
        return span.traceId();
    }

    @Override
    public String spanId() {
        return span.id();
    }

    @Override
    public String parentId() {
        return span.parentId();
    }

    @Override
    public String tag(String key) {
        Map<String, String> tags = span.tags();
        if (tags == null) {
            return null;
        }
        return tags.get(key);
    }

    @Override
    public Map<String, String> tags() {
        return span.tags();
    }

    @Override
    public int tagCount() {
        Map<String, String> tags = span.tags();
        if (tags == null) {
            return 0;
        }
        return tags.size();
    }

    @Override
    public String remoteServiceName() {
        return span.remoteServiceName();
    }

    @Override
    public String annotationValueAt(int i) {
        List<Annotation> annotations = span.annotations();
        if (annotations == null || annotations.size() <= i) {
            return null;
        }
        return annotations.get(i).value();
    }

    @Override
    public long timestamp() {
        return span.timestamp();
    }

    @Override
    public Long duration() {
        return span.duration();
    }

    @Override
    public int annotationCount() {
        List<Annotation> annotations = span.annotations();
        if (annotations == null) {
            return 0;
        }
        return annotations.size();
    }

    @Override
    public int remotePort() {
        return span.remoteEndpoint().portAsInt();
    }


    @Override
    public int localPort() {
        return span.localEndpoint().portAsInt();
    }

    @Override
    public String remoteIp() {
        return span.remoteEndpoint().ipv4();
    }

    @Override
    public String localIp() {
        return span.localEndpoint().ipv4();
    }

    @Override
    public String name() {
        return span.name();
    }

    @Override
    public String localServiceName() {
        return span.localServiceName();
    }

    @Override
    public Boolean shared() {
        return span.shared();
    }

}
