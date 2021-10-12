package com.megaease.easeagent.plugin.api.trace;

import com.megaease.easeagent.plugin.api.Context;

public interface TraceContext extends Context {
    void setCurrentTrace(Tracer tracer);
}
