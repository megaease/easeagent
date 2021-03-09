package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;

import static com.megaease.easeagent.config.ConfigConst.*;

public interface TraceProps {

    boolean isEnabled();

    KafkaOutputProps getOutput();

    interface KafkaOutputProps {

        boolean isEnabled();

        int getReportThread();

        int getMessageMaxBytes();

        String getTopic();

        int getQueuedMaxSpans();

        long getMessageTimeout();

        int getQueuedMaxSize();
    }

    static TraceProps newDefault(Configs configs) {
        return new Default(configs);
    }

    class Default implements TraceProps {
        private final KafkaOutputProps output;
        private volatile boolean enabled;

        public Default(Configs configs) {
            ConfigUtils.bindProp(TRACE_ENABLED, configs, Configs::getBoolean, v -> this.enabled = v);
            this.output = new KafkaOutputPropsImpl(configs);
        }


        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public KafkaOutputProps getOutput() {
            return output;
        }

        class KafkaOutputPropsImpl implements KafkaOutputProps {
            private volatile boolean enabled;
            private volatile String topic;
            private volatile int messageMaxBytes;
            private volatile int reportThread;
            private volatile int queuedMaxSpans;
            private volatile int queuedMaxSize;
            private volatile int messageTimeout;

            public KafkaOutputPropsImpl(Configs configs) {
                ConfigUtils.bindProp(TRACE_OUTPUT_ENABLED, configs, Configs::getBoolean, v -> this.enabled = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_TOPIC, configs, Configs::getString, v -> this.topic = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_MESSAGE_MAX_BYTES, configs, Configs::getInt, v -> this.messageMaxBytes = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_REPORT_THREAD, configs, Configs::getInt, v -> this.reportThread = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_QUEUED_MAX_SPANS, configs, Configs::getInt, v -> this.queuedMaxSpans = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_QUEUED_MAX_SIZE, configs, Configs::getInt, v -> this.queuedMaxSize = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_MESSAGE_TIMEOUT, configs, Configs::getInt, v -> this.messageTimeout = v);
            }

            @Override
            public boolean isEnabled() {
                return this.enabled;
            }

            @Override
            public int getReportThread() {
                return this.reportThread;
            }

            @Override
            public int getMessageMaxBytes() {
                return this.messageMaxBytes;
            }

            @Override
            public String getTopic() {
                return this.topic;
            }

            @Override
            public int getQueuedMaxSpans() {
                return this.queuedMaxSpans;
            }

            @Override
            public long getMessageTimeout() {
                return this.messageTimeout;
            }

            @Override
            public int getQueuedMaxSize() {
                return this.queuedMaxSize;
            }
        }
    }
}
