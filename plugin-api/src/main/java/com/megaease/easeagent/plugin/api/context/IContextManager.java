package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.InitializeContext;

public interface IContextManager {
    /**
     * Get current context or create a context
     * @return context
     */
    InitializeContext getContext();
}
