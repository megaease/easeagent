package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class LogDataLoggerPatternConverter extends NamePatternConverter {
    /**
     * Create a new pattern converter.
     *
     * @param options options, may be null.
     */
    public LogDataLoggerPatternConverter(String[] options) {
        super("Logger", "logger", options);
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        abbreviate(event.getLocation(), toAppendTo);
    }
}
