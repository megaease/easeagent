package com.megaease.easeagent.zipkin.impl;

import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.plugin.api.trace.Scope;

public class ScopeImpl implements Scope {
    private final CurrentTraceContext.Scope scope;

    public ScopeImpl(CurrentTraceContext.Scope scope) {
        this.scope = scope;
    }

    @Override
    public void close() {
        scope.close();
    }
}
