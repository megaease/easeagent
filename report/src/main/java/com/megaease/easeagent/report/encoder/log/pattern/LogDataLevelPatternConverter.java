package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class LogDataLevelPatternConverter extends LogDataPatternConverter {
    /**
     * Create a new pattern converter.
     */
    protected LogDataLevelPatternConverter() {
        super("Level", "level");
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        toAppendTo.append(event.getSeverityText());
    }
}
