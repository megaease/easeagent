package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class SimpleMessageConverter extends LogDataPatternConverter {
    public static final SimpleMessageConverter INSTANCE = new SimpleMessageConverter();
    /**
     * Create a new pattern converter.
     */
    protected SimpleMessageConverter() {
        super("msg", "msg");
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        toAppendTo.append(event.getBody().asString());
    }
}
