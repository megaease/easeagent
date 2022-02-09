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

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.elasticsearch.ElasticsearchPlugin;
import com.megaease.easeagent.plugin.elasticsearch.points.ElasticsearchPerformRequestPoints;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import static com.megaease.easeagent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils.REQUEST;

@AdviceTo(value = ElasticsearchPerformRequestPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestMetricsInterceptor extends ElasticsearchBaseMetricsInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Request request = (Request) methodInfo.getArgs()[0];
        context.put(REQUEST, request);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Response response = (Response) methodInfo.getRetValue();
        Request request = (Request) methodInfo.getArgs()[0];
        boolean success = ElasticsearchCtxUtils.checkSuccess(response, methodInfo.getThrowable());
        this.elasticsearchMetric.collectMetric(ElasticsearchCtxUtils.getIndex(request.getEndpoint()),
            ContextUtils.getDuration(context), success);
    }
}
