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
import com.megaease.easeagent.plugin.utils.common.StringUtils;
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
        if (StringUtils.isEmpty(endpoint)) {
            return "";
        }
        String tmp = endpoint;
        if (!tmp.startsWith("/")) {
            tmp = "/" + tmp;
        }
        int end = tmp.indexOf("/", 1);
        String index;
        if (end < 0) {
            index = tmp.substring(1);
        } else if (end > 0) {
            index = tmp.substring(1, end);
        } else {
            index = tmp.substring(1);
        }
        if (index.startsWith("_") || index.startsWith("-") || index.startsWith("+")) {
            return "";
        }
        return index;
    }

    public static boolean checkSuccess(Response response, Throwable throwable) {
        if (throwable != null) {
            return false;
        }
        if (response == null) {
            return false;
        }
        return response.getStatusLine().getStatusCode() == 200
            || response.getStatusLine().getStatusCode() == 201;
    }

    public static void finishSpan(Response response, Throwable throwable, Context context) {
        Span span = context.get(SPAN);
        if (span == null) {
            return;
        }
        if (throwable != null) {
            span.error(throwable);
            span.tag("error", throwable.getMessage());
        } else {
            if (!checkSuccess(response, null)) {
                if (response != null) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    span.tag("error", String.valueOf(statusCode));
                } else {
                    span.tag("error", "unknown");
                }

            }
        }
        span.finish();
        context.remove(SPAN);
    }
}
