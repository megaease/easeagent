package com.megaease.easeagent.core.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.HashMap;
import java.util.Map;


public class SessionContext implements Context, TraceContext {
    private Tracing tracing = NoOpTracer.NO_OP_TRACING;
    private Map<Object, Object> context = new HashMap<>();

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing currentTracing() {
        return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public <V> V getValue(Object key) {
        Object v = context.get(key);
        return v == null ? null : (V) v;
    }

    @Override
    public <V> V remove(Object key) {
        Object v = context.remove(key);
        return v == null ? null : (V) v;
    }

    @Override
    public AsyncContext exportAsync(Request request) {
        AsyncContext asyncContext = currentTracing().exportAsync(request);
        asyncContext.putContext(context);
        return asyncContext;
    }

    @Override
    public void importAsync(AsyncContext snapshot) {
        currentTracing().importAsync(snapshot);
        context.putAll(snapshot.getContext());
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        return currentTracing().nextProgress(request);
    }

    @Override
    public void importProgress(Request request) {
        currentTracing().importProgress(request);
    }

    @Override
    public Map<Object, Object> clear() {
        this.tracing = NoOpTracer.NO_OP_TRACING;
        Map<Object, Object> old = this.context;
        this.context = new HashMap<>();
        return old;
    }

    @Override
    public void setCurrentTracing(Tracing tracing) {
        this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }
}
