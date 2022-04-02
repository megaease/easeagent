package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class LogDataLineSeparatorPatternConverter extends LogDataPatternConverter {
    public static final LogDataLineSeparatorPatternConverter INSTANCE = new LogDataLineSeparatorPatternConverter();
    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        toAppendTo.append(System.lineSeparator());
    }
}
