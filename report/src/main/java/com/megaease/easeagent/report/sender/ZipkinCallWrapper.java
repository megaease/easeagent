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
package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Callback;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ZipkinCallWrapper<V> implements Call<V> {
    private final zipkin2.Call<V> call;

    public ZipkinCallWrapper(zipkin2.Call<V> call) {
        this.call = call;
    }

    @Override
    public V execute() throws IOException {
        try {
            return call.execute();
        } catch (Exception e) {
            log.warn("Call exception: {}", e.getMessage());
            throw new IOException();
        }
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
