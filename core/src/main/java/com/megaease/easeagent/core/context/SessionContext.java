package com.megaease.easeagent.core.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.HashMap;
import java.util.Map;


public class SessionContext implements Context, TraceContext {
    private Tracing tracing = NoOpTracer.NO_OP_TRACING;
    private Map<Object, Object> context = new HashMap<>();
    private Map<Object, Integer> entered = new HashMap<>();

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing currentTracing() {
        return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public <V> V get(Object key) {
        Object v = context.get(key);
        return v == null ? null : (V) v;
    }

    @Override
    public <V> V remove(Object key) {
        Object v = context.remove(key);
        return v == null ? null : (V) v;
    }

    @Override
    public <V> V put(Object key, V value) {
        context.put(key, value);
        return value;
    }

    @Override
    public int enter(Object key) {
        Integer count = entered.get(key);
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        entered.put(key, count);
        return count;
    }

    @Override
    public int out(Object key) {
        Integer count = entered.get(key);
        if (count == null) {
            return 0;
        }
        entered.put(key, count - 1);
        return count;
    }

    @Override
    public AsyncContext exportAsync(Request request) {
        AsyncContext asyncContext = currentTracing().exportAsync(request);
        asyncContext.putAll(context);
        return asyncContext;
    }

    @Override
    public Span importAsync(AsyncContext snapshot) {
        Span span = currentTracing().importAsync(snapshot);
        context.putAll(snapshot.getAll());
        return span;
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        ProgressContext progressContext = currentTracing().nextProgress(request);
        String[] fields = TransparentTransmission.getFields();
        if (TransparentTransmission.isEmpty(fields)) {
            return progressContext;
        }
        for (String field : fields) {
            Object o = context.get(field);
            if (o != null && (o instanceof String)) {
                progressContext.setHeader(field, (String) o);
            }
        }
        return progressContext;
    }

    @Override
    public ProgressContext importProgress(Request request) {
        ProgressContext progressContext = currentTracing().importProgress(request);
        String[] fields = TransparentTransmission.getFields();
        if (TransparentTransmission.isEmpty(fields)) {
            return progressContext;
        }
        for (String field : fields) {
            String value = request.header(field);
            progressContext.setHeader(field, value);
            context.put(field, value);
        }
        return progressContext;
    }

    @Override
    public Map<Object, Object> clear() {
        this.tracing = NoOpTracer.NO_OP_TRACING;
        Map<Object, Object> old = this.context;
        this.context = new HashMap<>();
        this.entered = new HashMap<>();
        return old;
    }

    @Override
    public void setCurrentTracing(Tracing tracing) {
        this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }
}
