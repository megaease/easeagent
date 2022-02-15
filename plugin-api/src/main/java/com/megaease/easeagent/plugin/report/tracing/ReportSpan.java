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
package com.megaease.easeagent.plugin.report.tracing;

import java.util.List;
import java.util.Map;

/**
 * borrow form zipkin2.Span
 */
@SuppressWarnings("unused")
public interface ReportSpan {
    /**
     * span base
     */
    String traceId();

    String parentId();

    /** spanId */
    String id();

    /** Span.Kind.name */
    String kind();

    /**
     * Span name in lowercase, rpc method for example.
     *
     * <p>Conventionally, when the span name isn't known, name = "unknown".
     */
    String name();

    /**
    * Epoch microseconds of the start of this span, possibly zero if this an incomplete span.
    */
    long timestamp();

    /**
    * Measurement in microseconds of the critical path, if known. Durations of less than one
    * microsecond must be rounded up to 1 microsecond.
    */
    long duration();

    /**
     * True if we are contributing to a span started by another tracer (ex on a different host).
     * Defaults to null. When set, it is expected for {@link #kind()} to be Kind#SERVER}.
     */
    boolean shared();

    /** True is a request to store this span even if it overrides sampling policy. */
    boolean debug();

    /**
    * The host that recorded this span, primarily for query by service name.
    */
    Endpoint localEndpoint();

    /**
    * The host that recorded this span, primarily for query by service name.
    */
    Endpoint remoteEndpoint();

    /**
     * annotation
     */
    List<Annotation> annotations();

    /**
     * tags
     */
    Map<String, String> tags();

    String tag(String key);

    default boolean hasError() {
        return tags().containsKey("error");
    }

    default String errorInfo() {
        return tags().get("error");
    }

    /**
     * global
     */
    String type();
    String service();
    String system();

    String localServiceName();
    String remoteServiceName();
}
