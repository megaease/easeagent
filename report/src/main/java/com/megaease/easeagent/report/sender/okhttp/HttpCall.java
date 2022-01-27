package com.megaease.easeagent.report.sender.okhttp;

import java.io.IOException;

import com.megaease.easeagent.plugin.report.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

final class HttpCall implements Callback<Void> {

    final okhttp3.Call call;

    HttpCall(okhttp3.Call call) {
        this.call = call;
    }

    @Override
    public Void execute() throws IOException {
        parseResponse(call.execute());
        return null;
    }

    // from zipkin-reporter-java
    static void parseResponse(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            if (response.isSuccessful()) {
                return;
            } else {
                throw new RuntimeException("response failed: " + response);
            }
        }
        try {
            BufferedSource content = responseBody.source();
            if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
                content = Okio.buffer(new GzipSource(responseBody.source()));
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    "response for " + response.request().tag() + " failed: " + content.readUtf8());
            }
        } finally {
            responseBody.close();
        }
    }
}
