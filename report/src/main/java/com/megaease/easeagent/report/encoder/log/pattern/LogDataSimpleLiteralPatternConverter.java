package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class LogDataSimpleLiteralPatternConverter extends LogDataPatternConverter {
    public static final LogDataPatternConverter UNKNOWN = new SimpleLiteralConverter("-Unknown Pattern-");

    LogEventPatternConverter converter;

    /**
     * Create a new pattern converter.
     */
    public LogDataSimpleLiteralPatternConverter(LogEventPatternConverter converter) {
        super("SimpleLiteral", "literal");
        this.converter = converter;
    }

    @Override
    public void format(final Object obj, final StringBuilder output) {
        this.converter.format(obj, output);
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        this.format((Object) event, toAppendTo);
    }

    public static class SimpleLiteralConverter extends LogDataPatternConverter {
        String literal;

        /**
         * Constructs an instance of LoggingEventPatternConverter.
         */
        protected SimpleLiteralConverter(String literal) {
            super("SimpleLiteral", "literal");
            this.literal = literal;
        }

        @Override
        public void format(AgentLogData event, StringBuilder toAppendTo) {
            toAppendTo.append(this.literal);
        }

        @Override
        public void format(LogEvent event, StringBuilder toAppendTo) {
            toAppendTo.append(this.literal);
        }
    }
}
