package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.Configs;

public class TracePropsImpl implements TraceProps {
    private final Configs configs;
    private final KafkaOutputPropsImpl kafkaOutputProps;

    public TracePropsImpl(Configs configs) {
        this.configs = configs;
        this.kafkaOutputProps = new KafkaOutputPropsImpl();
    }

    @Override
    public boolean isBraveFormat() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public KafkaOutputProps getOutput() {
        return this.kafkaOutputProps;
    }

    class KafkaOutputPropsImpl implements KafkaOutputProps {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public int getReportThread() {
            return 0;
        }

        @Override
        public int getMessageMaxBytes() {
            return 0;
        }

        @Override
        public String getTopic() {
            return null;
        }

        @Override
        public int getQueuedMaxSpans() {
            return 0;
        }

        @Override
        public long getMessageTimeout() {
            return 0;
        }

        @Override
        public int getQueuedMaxSize() {
            return 0;
        }
    }
}
