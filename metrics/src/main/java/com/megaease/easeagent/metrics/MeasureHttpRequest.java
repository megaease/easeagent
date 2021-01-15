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

 package com.megaease.easeagent.metrics;

import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Injection.Provider(Provider.class)
public abstract class MeasureHttpRequest extends HttpServletService {
    @AdviceTo(Service.class)
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

    static class Service {
        private static final String ALL = "All";
        private static final String REQUEST_THROUGHPUT = "request_throughput";
        private static final String REQUEST_NAME = "request_name";
        private static final String URL = "url";
        private static final String HTTP_CODE = "http_code";
        private static final String REQUEST_ERROR_THROUGHPUT = "request_error_throughput";

        private final CallTrace trace;
        private final Metrics metrics;

        @Injection.Autowire
        Service(CallTrace trace, Metrics metrics) {
            this.trace = trace;
            this.metrics = metrics;
        }

        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin Class<?> aClass, @Advice.Origin("#m") String method) {
            return Context.pushIfRoot(trace, aClass, method);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Argument(0) HttpServletRequest request,
                  @Advice.Argument(1) HttpServletResponse response,
                  @Advice.Enter boolean pushed) {
            if (!pushed) return;

            final Context context = trace.pop().context();
            final String signature = context.next == null ? context.signature : context.next.signature;

            final String uri = request.getRequestURL().toString();
            final int status = response.getStatus();
            final String code = Integer.toString(status);

            metrics.meter(REQUEST_THROUGHPUT).tag(REQUEST_NAME, signature).tag(URL, uri).tag(HTTP_CODE, code).get().mark();

            // TODO lazy calculation in streaming
            metrics.meter(REQUEST_THROUGHPUT).tag(REQUEST_NAME, signature).tag(URL, uri).get().mark();
            metrics.meter(REQUEST_THROUGHPUT).tag(REQUEST_NAME, ALL).tag(URL, ALL).get().mark();
            metrics.meter(REQUEST_THROUGHPUT).tag(REQUEST_NAME, ALL).tag(URL, ALL).tag(HTTP_CODE, code).get().mark();
            if (status >= 400) {
                metrics.meter(REQUEST_ERROR_THROUGHPUT).tag(REQUEST_NAME, signature).tag(URL, uri).get().mark();
                metrics.meter(REQUEST_ERROR_THROUGHPUT).tag(REQUEST_NAME, ALL).tag(URL, ALL).get().mark();
            }
        }
    }
}
