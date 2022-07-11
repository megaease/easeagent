/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.megaease.easeagent.report.sender.okhttp;

import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Callback;
import okhttp3.Response;

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
        try (Response response = call.execute()) {
            parseResponse(response);
        }
        return null;
    }

    @Override
    public void enqueue(Callback<Void> delegate) {
        call.enqueue(new V2CallbackAdapter<>(delegate));
    }

    static void parseResponse(Response response) throws IOException {
        if (response.isSuccessful()) {
            return;
        }
        throw new IOException("response failed: " + response);
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

        /**
         * Note: this runs on the {@link okhttp3.OkHttpClient#dispatcher() dispatcher} thread!
         */
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
