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
 */

package com.megaease.easeagent.plugin.api.config;

public interface ConfigConst {
    String PLUGIN = "plugin";
    String PLUGIN_GLOBAL = "global";
    String DELIMITER = ".";
    String PLUGIN_PREFIX = PLUGIN + DELIMITER;
    String PLUGIN_FORMAT = join(PLUGIN, "%s", "%s", "%s", "%s");//plugin.<Domain>.<Namespace>.<ServiceId>.<Properties>
    String SERVICE_NAME = "name";
    String SYSTEM_NAME = "system";

    // domain
    String OBSERVABILITY = "observability";
    String INTEGRABILITY = "integrability";
    String GLOBAL_CANARY_LABELS = "globalCanaryHeaders";

    // ServiceId
    String METRIC_SERVICE_ID = "metric";
    String TRACING_SERVICE_ID = "tracing";
    String SERVICE_ID_ENABLED_KEY = "enabled";

    static String join(String... texts) {
        return String.join(DELIMITER, texts);
    }

    static String[] split(String text) {
        return text.split("\\" + DELIMITER);
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
        String KEY_COMM_ENABLED = "enabled";
        String KEY_COMM_SAMPLED_BY_QPS = "sampledByQPS";
        String KEY_COMM_OUTPUT = "output";
        String KEY_COMM_TAG = "tag";
        String KEY_COMM_SERVICE_PREFIX = "servicePrefix";
        String KEY_COMM_INTERVAL = "interval";
        String KEY_COMM_INTERVAL_UNIT = "intervalUnit";
        String KEY_COMM_TOPIC = "topic";
        String KEY_COMM_APPEND_TYPE = "appendType";

        String OUTPUT = join(OBSERVABILITY, "outputServer");

        String OUTPUT_SERVERS = join(OUTPUT, "bootstrapServer");
        String OUTPUT_TIMEOUT = join(OUTPUT, "timeout");
        String OUTPUT_ENABLED = join(OUTPUT, "enabled");

        String OUTPUT_SECURITY_PROTOCOL = join(OUTPUT, "security.protocol");
        String OUTPUT_SSL_KEYSTORE_TYPE = join(OUTPUT, "ssl.keystore.type");
        String OUTPUT_KEY = join(OUTPUT, "ssl.keystore.key");
        String OUTPUT_CERT = join(OUTPUT, "ssl.keystore.certificate.chain");
        String OUTPUT_TRUST_CERT = join(OUTPUT, "ssl.truststore.certificates");
        String OUTPUT_TRUST_CERT_TYPE = join(OUTPUT, "ssl.truststore.type");
        String OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM = join(OUTPUT, "ssl.endpoint.identification.algorithm");

        String METRICS = join(OBSERVABILITY, "metrics");
        String TRACE = join(OBSERVABILITY, "tracings");

        String METRICS_ENABLED = join(METRICS, "enabled");

        String TRACE_ENABLED = join(TRACE, "enabled");
        String TRACE_SAMPLED_BY_QPS = join(TRACE, KEY_COMM_SAMPLED_BY_QPS);

        String TRACE_OUTPUT = join(TRACE, KEY_COMM_OUTPUT);
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

    }

    interface Plugin {
        String OBSERVABILITY_GLOBAL_METRIC_ENABLED = join(PLUGIN, OBSERVABILITY, PLUGIN_GLOBAL, METRIC_SERVICE_ID, SERVICE_ID_ENABLED_KEY);
        String OBSERVABILITY_GLOBAL_TRACING_ENABLED = join(PLUGIN, OBSERVABILITY, PLUGIN_GLOBAL, TRACING_SERVICE_ID, SERVICE_ID_ENABLED_KEY);
    }

    interface Namespace {
        String ASYNC = "async";
        String ELASTICSEARCH = "elasticsearch";
        String HTTP_SERVLET = "httpServlet";
        String JDBC = "jdbc";
        String JDBC_CONNECTION = "jdbcConnection";
        String JDBC_STATEMENT = "jdbcStatement";
        String KAFKA = "kafka";
        String RABBITMQ = "rabbitmq";
        String REDIS = "redis";
        String SERVICE_NAME = "serviceName";
        String HEALTH = "health";
        String ACCESS = "access";
        String SPRING_GATEWAY = "springGateway";
        String MD5_DICTIONARY = "md5Dictionary";
        // -------------  request  ------------------
        String HTTPCLIENT = "httpclient";
        String OK_HTTP = "okHttp";
        String WEB_CLIENT = "webclient";
        String FEIGN_CLIENT = "feignClient";
        String REST_TEMPLATE = "resTemplate";

        String FORWARDED = "forwarded";
    }

    interface PluginID {
        String TRACING_INIT = "tracingInit";
        String TRACING = "tracing";
        String METRIC = "metric";
        String REDIRECT = "redirect";
        String FORWARDED = "forwarded";
    }
}
