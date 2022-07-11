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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

public class AsyncResponse4TraceListener implements ResponseListener {

    private final ResponseListener delegate;
    private final AsyncContext asyncContext;


    public AsyncResponse4TraceListener(ResponseListener delegate, AsyncContext asyncContext) {
        this.delegate = delegate;
        this.asyncContext = asyncContext;
    }

    @Override
    public void onSuccess(Response response) {
        try {
            this.delegate.onSuccess(response);
        } finally {
            this.process(response, null);
        }
    }

    @Override
    public void onFailure(Exception exception) {
        try {
            this.delegate.onFailure(exception);
        } finally {
            this.process(null, exception);
        }
    }

    private void process(Response response, Exception exception) {
        try (Cleaner ignored = asyncContext.importToCurrent()) {
            Context context = EaseAgent.getContext();
            ElasticsearchCtxUtils.finishSpan(response, exception, context);
        }
    }
}
