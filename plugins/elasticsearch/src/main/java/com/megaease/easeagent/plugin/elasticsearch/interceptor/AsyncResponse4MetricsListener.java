/*
 * Copyright (c) 2021 MegaEase
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
 */

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

import static com.megaease.easeagent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils.REQUEST;

public class AsyncResponse4MetricsListener implements ResponseListener {

    private final ResponseListener delegate;
    private final AsyncContext asyncContext;
    private final ElasticsearchMetric elasticsearchMetric;


    public AsyncResponse4MetricsListener(ResponseListener delegate, AsyncContext asyncContext, ElasticsearchMetric elasticsearchMetric) {
        this.delegate = delegate;
        this.asyncContext = asyncContext;
        this.elasticsearchMetric = elasticsearchMetric;
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
            Request request = context.get(REQUEST);
            long duration = ContextUtils.getDuration(context);
            boolean success = ElasticsearchCtxUtils.checkSuccess(response, exception);
            this.elasticsearchMetric.collectMetric(ElasticsearchCtxUtils
                .getIndex(request.getEndpoint()), duration, success);
        }
    }
}
