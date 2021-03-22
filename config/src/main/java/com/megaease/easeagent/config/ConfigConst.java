package com.megaease.easeagent.config;

public interface ConfigConst {
    String DELIMITER = ".";
    String SERVICE_NAME = "name";
    String OBSERVABILITY = "observability";
    String CANARY = "canary";

    static String join(String... texts) {
        return String.join(DELIMITER, texts);
    }

    interface Canary {
        String FILTER_HEADERS = join(CANARY, "filter", "headers");

        static String extractHeaderName(String full) {
            String prefix = FILTER_HEADERS + DELIMITER;
            int idx = full.indexOf(prefix);
            if (idx < 0) {
                return null;
            }
            String remain = full.substring(idx + prefix.length());
            return remain.split("\\" + DELIMITER)[0];
        }
    }

    interface Observability {
        String OUTPUT = join(OBSERVABILITY, "outputServer");

        String OUTPUT_SERVERS = join(OUTPUT, "bootstrapServer");
        String OUTPUT_TIMEOUT = join(OUTPUT, "timeout");
        String OUTPUT_ENABLED = join(OUTPUT, "enabled");

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
