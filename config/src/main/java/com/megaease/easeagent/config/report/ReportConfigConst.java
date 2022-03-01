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
package com.megaease.easeagent.config.report;

@SuppressWarnings("unused")
public class ReportConfigConst {
    private ReportConfigConst() {}

    public static final String KAFKA_SENDER_NAME = "kafka";
    public static final String METRIC_KAFKA_SENDER_NAME = "metricKafka";
    public static final String CONSOLE_SENDER_NAME = "console";
    public static final String ZIPKIN_SENDER_NAME = "http";

    public static final String SPAN_JSON_ENCODER_NAME = "SpanJsonEncoder";
    public static final String METRIC_JSON_ENCODER_NAME = "MetricJsonEncoder";
    public static final String HTTP_SPAN_JSON_ENCODER_NAME = "HttpSpanJsonEncoder";

    static final String DELIMITER = ".";
    public static final String TOPIC_KEY = "topic";
    public static final String APPENDER_KEY = "appenderName";

    public static final String ENABLED_KEY = "enabled";
    public static final String SENDER_KEY = "sender";
    public static final String ENCODER_KEY = "encoder";
    public static final String NAME_KEY = "appendType";
    public static final String INTERVAL_KEY = "interval";

    /**
     * Reporter v2 configuration
     */
    // -- lv1 --
    public static final String REPORT = "reporter";
    // ---- lv2 ----
    public static final String OUTPUT_SERVER_V2 = join(REPORT, "outputServer");
    public static final String TRACE_V2 = join(REPORT, "tracing");
    public static final String METRIC_V2 = join(REPORT, "metric");
    public static final String GENERAL = join(REPORT, "general");
    // ------ lv3 ------
    public static final String BOOTSTRAP_SERVERS = join(OUTPUT_SERVER_V2, "bootstrapServer");
    public static final String OUTPUT_SERVERS_ENABLE = join(OUTPUT_SERVER_V2, ENABLED_KEY);
    public static final String OUTPUT_SERVERS_TIMEOUT = join(OUTPUT_SERVER_V2, "timeout");

    public static final String OUTPUT_SECURITY_PROTOCOL_V2 = join(OUTPUT_SERVER_V2, "security.protocol");
    public static final String OUTPUT_SERVERS_SSL = join(OUTPUT_SERVER_V2, "ssl");

    public static final String GENERAL_SENDER = join(GENERAL, SENDER_KEY);
    public static final String GENERAL_ENCODER = join(GENERAL, ENCODER_KEY);

    public static final String TRACE_SENDER = join(TRACE_V2, SENDER_KEY);
    public static final String TRACE_ENCODER = join(TRACE_V2, ENCODER_KEY);
    public static final String TRACE_ASYNC = join(TRACE_V2, "output");

    public static final String METRIC_SENDER = join(METRIC_V2, SENDER_KEY);
    public static final String METRIC_ENCODER = join(METRIC_V2, ENCODER_KEY);
    public static final String METRIC_ASYNC = join(METRIC_V2, "output");

    // -------- lv4  --------
    public static final String GENERAL_SENDER_NAME = join(GENERAL_SENDER, NAME_KEY);
    public static final String GENERAL_SENDER_ENABLED = join(GENERAL_SENDER, ENABLED_KEY);

    public static final String TRACE_SENDER_NAME = join(TRACE_SENDER, NAME_KEY);
    public static final String TRACE_SENDER_ENABLED_V2 = join(TRACE_SENDER, ENABLED_KEY);
    public static final String TRACE_SENDER_TOPIC_V2 = join(TRACE_SENDER, TOPIC_KEY);

    public static final String TRACE_ASYNC_MESSAGE_MAX_BYTES_V2 = join(TRACE_ASYNC, "messageMaxBytes");
    public static final String TRACE_ASYNC_REPORT_THREAD_V2 = join(TRACE_ASYNC, "reportThread");
    public static final String TRACE_ASYNC_MESSAGE_TIMEOUT_V2 = join(TRACE_ASYNC, "messageTimeout");
    public static final String TRACE_ASYNC_QUEUED_MAX_SPANS_V2 = join(TRACE_ASYNC, "queuedMaxSpans");
    public static final String TRACE_ASYNC_QUEUED_MAX_SIZE_V2 = join(TRACE_ASYNC, "queuedMaxSize");

    public static final String METRIC_SENDER_NAME = join(METRIC_SENDER, NAME_KEY);
    public static final String METRIC_SENDER_ENABLED = join(METRIC_SENDER, ENABLED_KEY);
    public static final String METRIC_SENDER_TOPIC = join(METRIC_SENDER, TOPIC_KEY);
    public static final String METRIC_SENDER_APPENDER = join(METRIC_SENDER, APPENDER_KEY);

    public static final String METRIC_ASYNC_INTERVAL = join(METRIC_ASYNC, INTERVAL_KEY);

    public static final String OUTPUT_SSL_KEYSTORE_TYPE_V2 = join(OUTPUT_SERVERS_SSL, "keystore.type");
    public static final String OUTPUT_KEY_V2 = join(OUTPUT_SERVERS_SSL, "keystore.key");
    public static final String OUTPUT_CERT_V2 = join(OUTPUT_SERVERS_SSL, "keystore.certificate.chain");
    public static final String OUTPUT_TRUST_CERT_V2 = join(OUTPUT_SERVERS_SSL, "truststore.certificates");
    public static final String OUTPUT_TRUST_CERT_TYPE_V2 = join(OUTPUT_SERVERS_SSL, "truststore.type");
    public static final String OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM_V2 = join(OUTPUT_SERVERS_SSL, "endpoint.identification.algorithm");

    /**
     * Reporter v1 configuration
     */
    public static final String OBSERVABILITY = "observability";
    // ---- lv2 ----
    public static final String TRACING = join(OBSERVABILITY, "tracings");
    public static final String OUTPUT_SERVER_V1 = join(OBSERVABILITY, "outputServer");
    // ------ lv3 ------
    public static final String TRACE_OUTPUT_V1 = join(TRACING, "output");
    public static final String BOOTSTRAP_SERVERS_V1 = join(OUTPUT_SERVER_V1, "bootstrapServer");

    // --------- lv4 ---------
    public static final String TRACE_OUTPUT_ENABLED_V1 = join(TRACE_OUTPUT_V1, ENABLED_KEY);
    public static final String TRACE_OUTPUT_TOPIC_V1 = join(TRACE_OUTPUT_V1, TOPIC_KEY);
    public static final String TRACE_OUTPUT_TARGET_V1 = join(TRACE_OUTPUT_V1, "target");
    public static final String TRACE_OUTPUT_TARGET_ZIPKIN_URL = join(TRACE_OUTPUT_V1, "target.zipkinUrl");

    public static final String TRACE_OUTPUT_REPORT_THREAD_V1 = join(TRACE_OUTPUT_V1, "reportThread");
    public static final String TRACE_OUTPUT_MESSAGE_TIMEOUT_V1 = join(TRACE_OUTPUT_V1, "messageTimeout");
    public static final String TRACE_OUTPUT_QUEUED_MAX_SPANS_V1 = join(TRACE_OUTPUT_V1, "queuedMaxSpans");
    public static final String TRACE_OUTPUT_QUEUED_MAX_SIZE_V1 = join(TRACE_OUTPUT_V1, "queuedMaxSize");

    public static final String GLOBAL_METRIC = "plugin.observability.global.metric";
    public static final String GLOBAL_METRIC_ENABLED = join(GLOBAL_METRIC, ENABLED_KEY);
    public static final String GLOBAL_METRIC_TOPIC = join(GLOBAL_METRIC, TOPIC_KEY);
    public static final String GLOBAL_METRIC_APPENDER = join(GLOBAL_METRIC, "appendType");

    public static String join(String... texts) {
        return String.join(DELIMITER, texts);
    }
}
