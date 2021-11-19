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

package com.megaease.easeagent.plugin.utils.trace;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.utils.FirstEnterInterceptor;

public abstract class BaseHttpClientTracingInterceptor implements FirstEnterInterceptor {

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpRequest request = getRequest(methodInfo, context);
        ProgressContext progressContext = context.nextProgress(request);
        HttpUtils.handleReceive(progressContext.span().start(), request);
        context.put(getProgressKey(), progressContext);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ProgressContext progressContext = context.remove(getProgressKey());
        try {
            HttpResponse responseWrapper = getResponse(methodInfo, context);
            progressContext.finish(responseWrapper);
            HttpUtils.finish(progressContext.span(), responseWrapper);
        } finally {
            progressContext.scope().close();
        }
    }

    public abstract Object getProgressKey();

    protected abstract HttpRequest getRequest(MethodInfo methodInfo, Context context);

    protected abstract HttpResponse getResponse(MethodInfo methodInfo, Context context);

}
