package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.InitializeContext;

public interface IContextManager {
    /**
     * Get current context
     * @param tracingRoot when true, get or create tracing context
     *                    when false, get or create context
     *
     * @return context with tracing context
     */
    InitializeContext getContext(boolean tracingRoot);
}
