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

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.trace.TraceConst;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.nio.charset.StandardCharsets;

public class ElasticsearchCtxUtils {
    private static final String SPAN = ElasticsearchCtxUtils.class.getName() + "-Span";
    public static final String REQUEST = ElasticsearchCtxUtils.class.getName() + "-Request";

    @SneakyThrows
    public static void initSpan(MethodInfo methodInfo, Context context) {
        Request request = (Request) methodInfo.getArgs()[0];
        HttpEntity entity = request.getEntity();
        Span span = context.nextSpan();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("Elasticsearch");
        span.tag("es.index", getIndex(request.getEndpoint()));
        span.tag("es.operation", request.getMethod() + " " + request.getEndpoint());
        if (entity != null) {
            String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            span.tag("es.body", body);
        }
        span.start();
        context.put(SPAN, span);
        context.put(REQUEST, request);
    }

    public static String getIndex(String endpoint) {
        int end = endpoint.indexOf("/");
        if (end == 0) {
            end = endpoint.indexOf("/", 1);
        }
        if (end == -1) {
            return endpoint;
        }
        return endpoint.substring(0, end);
    }

    public static void finishSpan(Response response, Throwable throwable, Context context) {
        Span span = context.get(SPAN);
        if (span == null) {
            return;
        }
        if (throwable != null) {
            span.error(throwable);
        } else {
            int statusCode = response.getStatusLine().getStatusCode();
            span.tag(TraceConst.HTTP_TAG_STATUS_CODE, String.valueOf(statusCode));
            if (statusCode != 200) {
                span.tag(TraceConst.HTTP_TAG_ERROR, String.valueOf(statusCode));
            }
        }
        span.finish();
        context.remove(SPAN);
    }
}
