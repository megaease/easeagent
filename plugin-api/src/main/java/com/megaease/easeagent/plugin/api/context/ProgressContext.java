package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.Map;

public interface ProgressContext {
    Span span();

    Scope scope();

    void setHeader(String name, String value);

    Map<String, String> getHeader();

    AsyncContext async();
}
