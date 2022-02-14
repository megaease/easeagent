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
package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.plugin.report.zipkin.Annotation;
import com.megaease.easeagent.plugin.report.zipkin.Endpoint;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpanImpl;
import zipkin2.Span;
import zipkin2.Span.Kind;
import zipkin2.internal.Platform;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static brave.internal.codec.HexCodec.toLowerHex;
import static brave.internal.codec.HexCodec.writeHexLong;
import static java.lang.String.format;
import static java.util.logging.Level.FINEST;

@SuppressWarnings("unused")
public class ReportSpanBuilder extends ReportSpanImpl.Builder {
    static final Endpoint EMPTY_ENDPOINT = endpoint(zipkin2.Endpoint.newBuilder().build());

    public static ReportSpanBuilder newBuilder() {
        return new ReportSpanBuilder();
    }

    public ReportSpanBuilder clear() {
        traceId = null;
        parentId = null;
        id = null;
        kind = null;
        name = null;
        timestamp = 0L;
        duration = 0L;
        localEndpoint = null;
        remoteEndpoint = null;
        if (annotations != null) annotations.clear();
        if (tags != null) tags.clear();
        shared = false;
        debug = false;
        return this;
    }

    /**
     * Used to merge multiple incomplete spans representing the same operation on the same host. Do
     * not use this to merge spans that occur on different hosts.
     */
    public ReportSpanBuilder merge(Span source) {
        if (traceId == null) traceId = source.traceId();
        if (id == null) id = source.id();
        if (parentId == null) parentId = source.parentId();
        if (kind == null) kind = source.kind().name();
        if (name == null) name = source.name();
        if (timestamp == 0L) timestamp = source.timestampAsLong();
        if (duration == 0L) duration = source.durationAsLong();
        if (localEndpoint == null) {
            localEndpoint = endpoint(source.localEndpoint());
        } else if (source.localEndpoint() != null) {
            mergeEndpoint(localEndpoint, source.localEndpoint());
        }
        if (remoteEndpoint == null) {
            remoteEndpoint = endpoint(source.remoteEndpoint());
        } else if (source.remoteEndpoint() != null) {
            mergeEndpoint(remoteEndpoint, source.remoteEndpoint());
        }
        if (!source.annotations().isEmpty()) {
            if (annotations == null) {
                annotations = new ArrayList<>(source.annotations().size());
            }
            annotations.addAll(annotations(source.annotations()));
        }
        if (!source.tags().isEmpty()) {
            if (tags == null) tags = new TreeMap<>();
            tags.putAll(source.tags());
        }
        shared = source.debug();
        debug = source.debug();

        return this;
    }

    Annotation annotation(zipkin2.Annotation sa) {
        return new Annotation(sa.timestamp(), sa.value());
    }

    Collection<Annotation> annotations(Collection<zipkin2.Annotation> sa) {
        return sa.stream().map(this::annotation).collect(Collectors.toList());
    }

    public static Endpoint endpoint(zipkin2.Endpoint endpoint) {
        Endpoint e = new Endpoint();
        e.setPort(endpoint.portAsInt());
        e.setServiceName(endpoint.serviceName());
        e.setIpv4(endpoint.ipv4());
        e.setIpv6(endpoint.ipv6());
        return e;
    }

    public static void mergeEndpoint(Endpoint e, zipkin2.Endpoint source) {
        if (e.serviceName() == null) {
            e.setServiceName(source.serviceName());
        }
        if (e.ipv4() == null) {
            e.setIpv4(source.ipv4());
        }
        if (e.ipv6() == null) {
            e.setIpv6(source.ipv6());
        }
        if (e.port() == 0) {
            e.setPort(source.port());
        }
    }

    public String kind() {
        return kind;
    }

    public Endpoint localEndpoint() {
        return localEndpoint;
    }

    /**
     * @throws IllegalArgumentException if not lower-hex format
     * @see ReportSpan#id()
     */
    public ReportSpanBuilder traceId(String traceId) {
        this.traceId = normalizeTraceId(traceId);
        return this;
    }

    /**
     * Encodes 64 or 128 bits from the input into a hex trace ID.
     *
     * @param high Upper 64bits of the trace ID. Zero means the trace ID is 64-bit.
     * @param low Lower 64bits of the trace ID.
     * @throws IllegalArgumentException if both values are zero
     */
    public ReportSpanBuilder traceId(long high, long low) {
        if (high == 0L && low == 0L) throw new IllegalArgumentException("empty trace ID");
        char[] data = Platform.shortStringBuffer();
        int pos = 0;
        if (high != 0L) {
            writeHexLong(data, pos, high);
            pos += 16;
        }
        writeHexLong(data, pos, low);
        this.traceId = new String(data, 0, high != 0L ? 32 : 16);
        return this;
    }

    /**
     * Encodes 64 bits from the input into a hex parent ID. Unsets the {@link ReportSpan#parentId()} if
     * the input is 0.
     *
     * @see ReportSpan#parentId()
     */
    public ReportSpanBuilder parentId(long parentId) {
        this.parentId = parentId != 0L ? toLowerHex(parentId) : null;
        return this;
    }

    /**
     * @throws IllegalArgumentException if not lower-hex format
     * @see ReportSpan#parentId()
     */
    public ReportSpanBuilder parentId(String parentId) {
        if (parentId == null) {
            this.parentId = null;
            return this;
        }
        int length = parentId.length();
        if (length == 0) throw new IllegalArgumentException("parentId is empty");
        if (length > 16) throw new IllegalArgumentException("parentId.length > 16");
        if (validateHexAndReturnZeroPrefix(parentId) == length) {
            this.parentId = null;
        } else {
            this.parentId = length < 16 ? padLeft(parentId, 16) : parentId;
        }
        return this;
    }

    /**
     * Encodes 64 bits from the input into a hex span ID.
     *
     * @throws IllegalArgumentException if the input is zero
     * @see ReportSpan#id()
     */
    public ReportSpanBuilder id(long id) {
        if (id == 0L) throw new IllegalArgumentException("empty id");
        this.id = toLowerHex(id);
        return this;
    }

    /**
     * @throws IllegalArgumentException if not lower-hex format
     * @see ReportSpan#id()
     */
    public ReportSpanBuilder id(String id) {
        if (id == null) throw new NullPointerException("id == null");
        int length = id.length();
        if (length == 0) throw new IllegalArgumentException("id is empty");
        if (length > 16) throw new IllegalArgumentException("id.length > 16");
        if (validateHexAndReturnZeroPrefix(id) == 16) {
            throw new IllegalArgumentException("id is all zeros");
        }
        this.id = length < 16 ? padLeft(id, 16) : id;
        return this;
    }

    public ReportSpanBuilder kind(Kind kind) {
        this.kind = kind.name();
        return this;
    }

    public ReportSpanBuilder name(String name) {
        this.name = name == null || name.isEmpty() ? null : name.toLowerCase(Locale.ROOT);
        return this;
    }

    /** @see ReportSpan#timestamp() */
    public ReportSpanBuilder timestamp(long timestamp) {
        if (timestamp < 0L) timestamp = 0L;
        this.timestamp = timestamp;
        return this;
    }

    /** @see ReportSpan#timestamp() */
    public ReportSpanBuilder timestamp(Long timestamp) {
        if (timestamp == null || timestamp < 0L) timestamp = 0L;
        this.timestamp = timestamp;
        return this;
    }

    /** @see ReportSpan#duration() */
    public ReportSpanBuilder duration(long duration) {
        if (duration < 0L) duration = 0L;
        this.duration = duration;
        return this;
    }

    /** @see ReportSpan#duration() */
    public ReportSpanBuilder duration(Long duration) {
        if (duration == null || duration < 0L) duration = 0L;
        this.duration = duration;
        return this;
    }

    /** @see ReportSpan#localEndpoint() */
    public ReportSpanBuilder localEndpoint(Endpoint localEndpoint) {
        if (EMPTY_ENDPOINT.equals(localEndpoint)) {
            localEndpoint = null;
        }
        this.localEndpoint = localEndpoint;
        return this;
    }

    /** @see ReportSpan#remoteEndpoint() */
    public ReportSpanBuilder remoteEndpoint(Endpoint remoteEndpoint) {
        if (EMPTY_ENDPOINT.equals(remoteEndpoint)) remoteEndpoint = null;
        this.remoteEndpoint = remoteEndpoint;
        return this;
    }

    /** @see ReportSpan#annotations() */
    public ReportSpanBuilder addAnnotation(long timestamp, String value) {
        if (annotations == null) annotations = new ArrayList<>(2);
        annotations.add(new Annotation(timestamp, value));
        return this;
    }

    /** @see ReportSpan#annotations() */
    public ReportSpanBuilder clearAnnotations() {
        if (annotations == null) return this;
        annotations.clear();
        return this;
    }

    /** @see ReportSpan#tags() */
    public ReportSpanBuilder putTag(String key, String value) {
        if (tags == null) tags = new TreeMap<>();
        if (key == null) throw new NullPointerException("key == null");
        if (value == null) throw new NullPointerException("value of " + key + " == null");
        this.tags.put(key, value);
        return this;
    }

    /** @see ReportSpan#tags() */
    public ReportSpanBuilder clearTags() {
        if (tags == null) return this;
        tags.clear();
        return this;
    }

    /** @see ReportSpan#debug() */
    public ReportSpanBuilder debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /** @see ReportSpan#debug */
    public ReportSpanBuilder debug(Boolean debug) {
        if (debug != null) {
            return debug((boolean) debug);
        } else {
            return debug(false);
        }
    }

    /** @see ReportSpan#shared */
    public ReportSpanBuilder shared(boolean shared) {
        this.shared = shared;
        return this;
    }

    /** @see ReportSpan#shared */
    public ReportSpanBuilder shared(Boolean shared) {
        if (shared != null) {
            return shared((boolean) shared);
        } else {
            return shared(false);
        }
    }

    public ReportSpan build() {
        String missing = "";
        if (traceId == null) {
            missing += " traceId";
        }
        if (id == null) {
            missing += " id";
        }
        if (!"".equals(missing)) {
            throw new IllegalStateException("Missing :" + missing);
        }
        if (id.equals(parentId)) { // edge case, so don't require a logger field
            Logger logger = Logger.getLogger(ReportSpan.class.getName());
            if (logger.isLoggable(FINEST)) {
                logger.fine(format("undoing circular dependency: traceId=%s, spanId=%s", traceId, id));
            }
            parentId = null;
        }
        annotations = sortedList(annotations);
        // shared is for the server side, unset it if accidentally set on the client side
        if (this.shared && kind.equals(Kind.CLIENT.name())) {
            Logger logger = Logger.getLogger(ReportSpan.class.getName());
            if (logger.isLoggable(FINEST)) {
                logger.fine(format("removing shared flag on client: traceId=%s, spanId=%s", traceId, id));
            }
            shared(null);
        }
        return new ReportSpanImpl(this);
    }

    ReportSpanBuilder() {
    }

    /**
     * Returns a valid lower-hex trace ID, padded left as needed to 16 or 32 characters.
     *
     * @throws IllegalArgumentException if over-sized or not lower-hex
     */
    public static String normalizeTraceId(String traceId) {
        if (traceId == null) throw new NullPointerException("traceId == null");
        int length = traceId.length();
        if (length == 0) throw new IllegalArgumentException("traceId is empty");
        if (length > 32) throw new IllegalArgumentException("traceId.length > 32");
        int zeros = validateHexAndReturnZeroPrefix(traceId);
        if (zeros == length) throw new IllegalArgumentException("traceId is all zeros");
        if (length == 32 || length == 16) {
            if (length == 32 && zeros >= 16) return traceId.substring(16);
            return traceId;
        } else if (length < 16) {
            return padLeft(traceId, 16);
        } else {
            return padLeft(traceId, 32);
        }
    }

    static int validateHexAndReturnZeroPrefix(String id) {
        int zeros = 0;
        boolean inZeroPrefix = id.charAt(0) == '0';
        for (int i = 0, length = id.length(); i < length; i++) {
            char c = id.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                throw new IllegalArgumentException(id + " should be lower-hex encoded with no prefix");
            }
            if (c != '0') {
                inZeroPrefix = false;
            } else if (inZeroPrefix) {
                zeros++;
            }
        }
        return zeros;
    }

    static final String THIRTY_TWO_ZEROS;
    static {
        char[] zeros = new char[32];
        Arrays.fill(zeros, '0');
        THIRTY_TWO_ZEROS = new String(zeros);
    }

    static String padLeft(String id, int desiredLength) {
        int length = id.length();
        int numZeros = desiredLength - length;

        char[] data = Platform.shortStringBuffer();
        THIRTY_TWO_ZEROS.getChars(0, numZeros, data, 0);
        id.getChars(0, length, data, numZeros);

        return new String(data, 0, desiredLength);
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<? super T>> List<T> sortedList(List<T> in) {
        if (in == null || in.isEmpty()) {
            return Collections.emptyList();
        }

        if (in.size() == 1) {
            return Collections.singletonList(in.get(0));
        }

        Object[] array = in.toArray();
        Arrays.sort(array);

        // dedupe
        int j = 0;
        int i = 1;
        while (i < array.length) {
            if (!array[i].equals(array[j])) {
                array[++j] = array[i];
            }
            i++;
        }

        List result = Arrays.asList(i == j + 1 ? array : Arrays.copyOf(array, j + 1));
        return Collections.<T>unmodifiableList(result);
    }
}
