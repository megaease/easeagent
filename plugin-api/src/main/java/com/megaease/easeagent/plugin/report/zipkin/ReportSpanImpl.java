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
package com.megaease.easeagent.plugin.report.zipkin;

import java.util.*;

public class ReportSpanImpl implements ReportSpan {
    String traceId;
    String parentId;
    String id;
    String kind;
    String name;
    long timestamp;
    long duration;
    boolean shared;
    boolean debug;
    Endpoint localEndpoint;
    Endpoint remoteEndpoint;
    List<Annotation> annotations;
    Map<String, String> tags;

    String type;
    String service;
    String system;

    @Override
    public String traceId() {
        return traceId;
    }

    @Override
    public String parentId() {
        return parentId;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String kind() {
        return kind;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public boolean shared() {
        return shared;
    }

    @Override
    public boolean debug() {
        return debug;
    }

    @Override
    public Endpoint localEndpoint() {
        return localEndpoint;
    }

    @Override
    public Endpoint remoteEndpoint() {
        return remoteEndpoint;
    }

    @Override
    public List<Annotation> annotations() {
        return annotations;
    }

    @Override
    public Map<String, String> tags() {
        return tags;
    }

    @Override
    public String tag(String key) {
        return tags == null ? null : tags.get(key);
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String service() {
        return service;
    }

    @Override
    public String localServiceName() {
        return localEndpoint != null ? localEndpoint.serviceName() : null;
    }

    @Override
    public String remoteServiceName() {
        return remoteEndpoint != null ? remoteEndpoint.serviceName() : null;
    }

    @Override
    public String system() {
        return system;
    }

    public ReportSpanImpl(Builder builder) {
        traceId = builder.traceId;
        // prevent self-referencing spans
        parentId = builder.id.equals(builder.parentId) ? null : builder.parentId;
        id = builder.id;
        kind = builder.kind;
        name = builder.name;
        timestamp = builder.timestamp;
        duration = builder.duration;
        localEndpoint = builder.localEndpoint;
        remoteEndpoint = builder.remoteEndpoint;
        annotations = builder.annotations;
        tags = builder.tags == null ? Collections.emptyMap() : new LinkedHashMap<>(builder.tags);
        debug = builder.debug;
        shared = builder.shared;
    }

    public abstract static class Builder {
        protected String traceId;
        protected String parentId;
        protected String id;
        protected String kind;
        protected String name;
        protected long timestamp;   // zero means null
        protected long duration;    // zero means null
        protected Endpoint localEndpoint;
        protected Endpoint remoteEndpoint;
        protected List<Annotation> annotations;
        protected TreeMap<String, String> tags;
        protected boolean shared;
        protected boolean debug;
    }
}
