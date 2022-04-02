package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class NoOpPatternConverter extends LogDataPatternConverter {
    public final static NoOpPatternConverter INSTANCE = new NoOpPatternConverter("", "");

    /**
     * Create a new pattern converter.
     *
     * @param name  name for pattern converter.
     * @param style CSS style for formatted output.
     */
    protected NoOpPatternConverter(String name, String style) {
        super(name, style);
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
    }
}
