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

package com.megaease.easeagent.mock.report.impl;

import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import zipkin2.Span;
import com.megaease.easeagent.plugin.report.tracing.Annotation;

import javax.annotation.Nonnull;
import java.util.*;

public class ZipkinMockSpanImpl implements MockSpan {
    private static final Map<String, com.megaease.easeagent.plugin.api.trace.Span.Kind> KINDS;

    static {
        Map<String, com.megaease.easeagent.plugin.api.trace.Span.Kind> kinds = new HashMap<>();
        kinds.put(Span.Kind.CLIENT.name(), com.megaease.easeagent.plugin.api.trace.Span.Kind.CLIENT);
        kinds.put(Span.Kind.SERVER.name(), com.megaease.easeagent.plugin.api.trace.Span.Kind.SERVER);
        kinds.put(Span.Kind.PRODUCER.name(), com.megaease.easeagent.plugin.api.trace.Span.Kind.PRODUCER);
        kinds.put(Span.Kind.CONSUMER.name(), com.megaease.easeagent.plugin.api.trace.Span.Kind.CONSUMER);
        KINDS = Collections.unmodifiableMap(kinds);
    }


    private final ReportSpan span;

    public ZipkinMockSpanImpl(@Nonnull ReportSpan span) {
        this.span = span;
    }

    @Override
    public com.megaease.easeagent.plugin.api.trace.Span.Kind kind() {
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
    public boolean hasError() {
        return span.tags().containsKey("error");
    }

    @Override
    public String errorInfo() {
        return span.tags().get("error");
    }

    @Override
    public String remoteServiceName() {
        return span.remoteEndpoint().serviceName();
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
        return span.remoteEndpoint().port();
    }


    @Override
    public int localPort() {
        return span.localEndpoint().port();
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
