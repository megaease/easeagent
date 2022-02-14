/*
 * Copyright (c) 2021, MegaEase
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
package zipkin2.reporter.brave;
import brave.Span.Kind;
import brave.Tag;
import brave.handler.MutableSpan;
import brave.handler.MutableSpan.AnnotationConsumer;
import brave.handler.MutableSpan.TagConsumer;
import brave.handler.SpanHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import com.megaease.easeagent.report.trace.ReportSpanBuilder;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

final class ConvertSpanReporter implements Reporter<MutableSpan> {
    static final Logger logger = Logger.getLogger(ConvertSpanReporter.class.getName());
    static final Map<Kind, Span.Kind> BRAVE_TO_ZIPKIN_KIND = generateKindMap();

    final Reporter<ReportSpan> delegate;
    final Tag<Throwable> errorTag;

    ConvertSpanReporter(Reporter<ReportSpan> delegate, Tag<Throwable> errorTag) {
        this.delegate = delegate;
        this.errorTag = errorTag;
    }

    @Override public void report(MutableSpan span) {
        maybeAddErrorTag(span);
        ReportSpan converted = convert(span);
        delegate.report(converted);
    }

    static ReportSpan convert(MutableSpan span) {
        ReportSpanBuilder result = ReportSpanBuilder.newBuilder()
            .traceId(span.traceId())
            .parentId(span.parentId())
            .id(span.id())
            .name(span.name());

        long start = span.startTimestamp();
        long finish = span.finishTimestamp();
        result.timestamp(start);
        if (start != 0 && finish != 0L) {
            result.duration(Math.max(finish - start, 1));
        }

        // use ordinal comparison to defend against version skew
        Kind kind = span.kind();
        if (kind != null) {
            result.kind(BRAVE_TO_ZIPKIN_KIND.get(kind));
        }

        String localServiceName = span.localServiceName();
        String localIp = span.localIp();
        if (localServiceName != null || localIp != null) {
            zipkin2.Endpoint e = Endpoint.newBuilder()
                .serviceName(localServiceName)
                .ip(localIp)
                .port(span.localPort())
                .build();
            result.localEndpoint(ReportSpanBuilder.endpoint(e));
        }

        String remoteServiceName = span.remoteServiceName();
        String remoteIp = span.remoteIp();
        if (remoteServiceName != null || remoteIp != null) {
            zipkin2.Endpoint e = Endpoint.newBuilder()
                .serviceName(remoteServiceName)
                .ip(remoteIp)
                .port(span.remotePort())
                .build();
            result.remoteEndpoint(ReportSpanBuilder.endpoint(e));
        }

        span.forEachTag(Consumer.INSTANCE, result);
        span.forEachAnnotation(Consumer.INSTANCE, result);

        if (span.shared()) result.shared(true);
        if (span.debug()) result.debug(true);
        return result.build();
    }

    void maybeAddErrorTag(MutableSpan span) {
        // span.tag(key) iterates: check if we need to first!
        if (span.error() == null) return;
        if (span.tag("error") == null) errorTag.tag(span.error(), null, span);
    }

    @Override public String toString() {
        return delegate.toString();
    }

    /**
     * Overridden to avoid duplicates when added via {@link brave.Tracing.Builder#addSpanHandler(SpanHandler)}
     */
    @Override public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ConvertSpanReporter)) return false;
        return delegate.equals(((ConvertSpanReporter) o).delegate);
    }

    /**
     * Overridden to avoid duplicates when added via {@link brave.Tracing.Builder#addSpanHandler(SpanHandler)}
     */
    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    enum Consumer implements TagConsumer<ReportSpanBuilder>, AnnotationConsumer<ReportSpanBuilder> {
        INSTANCE;

        @Override public void accept(ReportSpanBuilder target, String key, String value) {
            target.putTag(key, value);
        }

        @Override public void accept(ReportSpanBuilder target, long timestamp, String value) {
            target.addAnnotation(timestamp, value);
        }
    }

    /**
     * This keeps the code maintenance free in the rare case there is disparity between Brave and
     * Zipkin kind values.
     */
    static Map<Kind, Span.Kind> generateKindMap() {
        Map<Kind, Span.Kind> result = new LinkedHashMap<>();
        // Note: Both Brave and Zipkin treat null kind as a local/in-process span
        for (Kind kind : Kind.values()) {
            try {
                result.put(kind, Span.Kind.valueOf(kind.name()));
            } catch (RuntimeException e) {
                logger.warning("Could not map Brave kind " + kind + " to Zipkin");
            }
        }
        return result;
    }
}
