package com.megaease.easeagent.report.sender.okhttp;

import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

import javax.annotation.Nonnull;
import java.io.IOException;

// from zipkin-reporter-java
final class HttpCall implements Call<Void> {

    final okhttp3.Call call;

    HttpCall(okhttp3.Call call) {
        this.call = call;
    }

    @Override
    public Void execute() throws IOException {
        parseResponse(call.execute());
        return null;
    }

    @Override
    public void enqueue(Callback<Void> delegate) {
        call.enqueue(new V2CallbackAdapter<>(delegate));
    }

    static void parseResponse(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            if (response.isSuccessful()) {
                return;
            } else {
                throw new IOException("response failed: " + response);
            }
        }
        BufferedSource content = null;
        try {
            if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
                content = Okio.buffer(new GzipSource(responseBody.source()));
            } else {
                content = responseBody.source();
            }
            if (!response.isSuccessful()) {
                throw new IOException(
                    "response for " + response.request().tag() + " failed: " + content.readUtf8());
            }
        } finally {
            if (content != null) {
                content.close();
            }
            responseBody.close();
        }
    }

    static class V2CallbackAdapter<V> implements okhttp3.Callback {
        final Callback<V> delegate;

        V2CallbackAdapter(Callback<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onFailure(@Nonnull okhttp3.Call call, @Nonnull IOException e) {
            delegate.onError(e);
        }

        /** Note: this runs on the {@link okhttp3.OkHttpClient#dispatcher() dispatcher} thread! */
        @Override
        public void onResponse(@Nonnull okhttp3.Call call, @Nonnull Response response) {
            try {
                parseResponse(response);
                delegate.onSuccess(null);
            } catch (Throwable e) {
                delegate.onError(e);
            }
        }
    }
}
