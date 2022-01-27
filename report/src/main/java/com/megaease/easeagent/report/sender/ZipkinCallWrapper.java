package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Callback;

import java.io.IOException;

public class ZipkinCallWrapper<V> implements Call<V> {
    private final zipkin2.Call<V> call;

    public ZipkinCallWrapper(zipkin2.Call<V> call) {
        this.call = call;
    }

    @Override
    public V execute() throws IOException {
        return call.execute();
    }

    @Override
    public void enqueue(Callback<V> cb) {
        zipkin2.Callback<V> zCb = new ZipkinCallbackWrapper<>(cb);
        this.call.enqueue(zCb);
    }

    static class ZipkinCallbackWrapper<V> implements zipkin2.Callback<V> {
        final Callback<V> delegate;

        ZipkinCallbackWrapper(Callback<V> cb) {
            this.delegate = cb;
        }

        @Override
        public void onSuccess(V value) {
            this.delegate.onSuccess(value);
        }

        @Override
        public void onError(Throwable t) {
            this.delegate.onError(t);
        }
    }
}
