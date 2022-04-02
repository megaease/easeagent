package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class LogDataThreadNamePatternConverter extends LogDataPatternConverter {
    public static final LogDataThreadNamePatternConverter INSTANCE = new LogDataThreadNamePatternConverter();
    /**
     * Create a new pattern converter.
     */
    protected LogDataThreadNamePatternConverter() {
        super("Thread", "thread");
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        toAppendTo.append(event.getThreadName());
    }
}
