/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.plugin.tools.trace;

import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;

public abstract class BaseHttpClientTracingInterceptor implements NonReentrantInterceptor {

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpRequest request = getRequest(methodInfo, context);
        RequestContext requestContext = context.clientRequest(request);
        HttpUtils.handleReceive(requestContext.span().start(), request);
        context.put(getProgressKey(), requestContext);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        RequestContext requestContext = context.remove(getProgressKey());
        if (requestContext == null) {
            return;
        }
        try {
            HttpResponse responseWrapper = getResponse(methodInfo, context);
            HttpUtils.save(requestContext.span(), responseWrapper);
            requestContext.finish(responseWrapper);
        } finally {
            requestContext.scope().close();
        }
    }

    @Override
    public int order() {
        // return Order.TRACING_APPEND.getOrder();
        return Order.TRACING.getOrder();
    }

    public abstract Object getProgressKey();

    protected abstract HttpRequest getRequest(MethodInfo methodInfo, Context context);

    protected abstract HttpResponse getResponse(MethodInfo methodInfo, Context context);
}
