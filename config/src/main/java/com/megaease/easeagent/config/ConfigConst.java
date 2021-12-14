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

package com.megaease.easeagent.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

public interface ConfigConst {
    String DELIMITER = ".";
    String SERVICE_NAME = "name";
    String SYSTEM_NAME = "system";
    String OBSERVABILITY = "observability";
    String GLOBAL_CANARY_LABELS = "globalCanaryHeaders";

    static String join(String... texts) {
        return String.join(DELIMITER, texts);
    }

    interface GlobalCanaryLabels {
        String SERVICE_HEADERS = join(GLOBAL_CANARY_LABELS, "serviceHeaders");

        static String extractHeaderName(String full) {
            String prefix = SERVICE_HEADERS + DELIMITER;
            int idx = full.indexOf(prefix);
            if (idx < 0) {
                return null;
            }
            String remain = full.substring(idx + prefix.length());
            String[] arr = remain.split("\\" + DELIMITER);
            if (arr.length < 3) {
                return null;
            }
            return arr[2];
        }
    }

    interface Observability {
        String OUTPUT = join(OBSERVABILITY, "outputServer");

        String OUTPUT_SERVERS = join(OUTPUT, "bootstrapServer");
        String OUTPUT_TIMEOUT = join(OUTPUT, "timeout");
        String OUTPUT_ENABLED = join(OUTPUT, "enabled");

        String OUTPUT_SECURITY_PROTOCOL = join(OUTPUT, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
        String OUTPUT_SSL_KEYSTORE_TYPE = join(OUTPUT, SslConfigs.SSL_KEYSTORE_TYPE_CONFIG);
        String OUTPUT_KEY = join(OUTPUT, SslConfigs.SSL_KEYSTORE_KEY_CONFIG);
        String OUTPUT_CERT = join(OUTPUT, SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG);
        String OUTPUT_TRUST_CERT = join(OUTPUT, SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG);
        String OUTPUT_TRUST_CERT_TYPE = join(OUTPUT, SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG);
        String OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM = join(OUTPUT, SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG);

        String METRICS = join(OBSERVABILITY, "metrics");
        String TRACE = join(OBSERVABILITY, "tracings");

        String METRICS_ENABLED = join(METRICS, "enabled");

        String TRACE_ENABLED = join(TRACE, "enabled");
        String TRACE_SAMPLED_BY_QPS = join(TRACE, "sampledByQPS");

        String TRACE_OUTPUT = join(TRACE, "output");
        String TRACE_OUTPUT_ENABLED = join(TRACE_OUTPUT, "enabled");
        String TRACE_OUTPUT_TOPIC = join(TRACE_OUTPUT, "topic");
        String TRACE_OUTPUT_REPORT_THREAD = join(TRACE_OUTPUT, "reportThread");
        String TRACE_OUTPUT_MESSAGE_MAX_BYTES = join(TRACE_OUTPUT, "messageMaxBytes");
        String TRACE_OUTPUT_MESSAGE_TIMEOUT = join(TRACE_OUTPUT, "messageTimeout");
        String TRACE_OUTPUT_QUEUED_MAX_SPANS = join(TRACE_OUTPUT, "queuedMaxSpans");
        String TRACE_OUTPUT_QUEUED_MAX_SIZE = join(TRACE_OUTPUT, "queuedMaxSize");

        String KEY_METRICS_ACCESS = "access";
        String KEY_METRICS_REQUEST = "request";
        String KEY_METRICS_JDBC_STATEMENT = "jdbcStatement";
        String KEY_METRICS_JDBC_CONNECTION = "jdbcConnection";
        String KEY_METRICS_RABBIT = "rabbit";
        String KEY_METRICS_KAFKA = "kafka";
        String KEY_METRICS_CACHE = "redis";
        String KEY_METRICS_JVM_GC = "jvmGc";
        String KEY_METRICS_JVM_MEMORY = "jvmMemory";
        String KEY_METRICS_MD5_DICTIONARY = "md5Dictionary";

        String KEY_TRACE_REQUEST = "request";
        String KEY_TRACE_REMOTE_INVOKE = "remoteInvoke";
        String KEY_TRACE_KAFKA = "kafka";
        String KEY_TRACE_JDBC = "jdbc";
        String KEY_TRACE_CACHE = "redis";
        String KEY_TRACE_RABBIT = "rabbit";

        String KEY_COMM_ENABLED = "enabled";
        String KEY_COMM_SERVICE_PREFIX = "servicePrefix";
        String KEY_COMM_INTERVAL = "interval";
        String KEY_COMM_TOPIC = "topic";
        String KEY_COMM_APPEND_TYPE = "appendType";
    }
}
