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

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.elasticsearch.ElasticsearchPlugin;
import com.megaease.easeagent.plugin.elasticsearch.points.ElasticsearchPerformRequestAsyncPoints;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.ResponseListener;

@AdviceTo(value = ElasticsearchPerformRequestAsyncPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestAsync4TraceInterceptor extends ElasticsearchBaseTraceInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ElasticsearchCtxUtils.initSpan(methodInfo, context);
        AsyncContext asyncContext = context.exportAsync();
        ResponseListener listener = (ResponseListener) methodInfo.getArgs()[1];
        ResponseListener asyncResponseListener = new AsyncResponse4TraceListener(listener, asyncContext);
        methodInfo.changeArg(1, asyncResponseListener);
    }

}
