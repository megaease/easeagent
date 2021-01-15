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

 package com.megaease.easeagent.requests;

import brave.sampler.Sampler;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
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
import java.util.Collections;
import java.util.Map;

@Injection.Provider(Provider.class)
public abstract class CaptureHttpRequest extends HttpServletService {

    @AdviceTo(Service.class)
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

    static class Service {
        private final CallTrace trace;
        private final Sampler sampler;
        private final Reporter reporter;

        @Injection.Autowire
        Service(CallTrace trace, Sampler sampler, Reporter reporter) {
            this.trace = trace;
            this.sampler = sampler;
            this.reporter = reporter;
        }

        @Advice.OnMethodEnter
        boolean enter(@Advice.Origin Class aClass, @Advice.Origin("#m") String method) {
            return sampler.isSampled(System.nanoTime()) && Context.pushIfRootCall(trace, aClass, method);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter boolean pushed, @Advice.Argument(0) HttpServletRequest request,
                  @Advice.Argument(1) HttpServletResponse response) {
            if (!pushed) return;

            reporter.report(
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    headers(request),
                    queries(request.getQueryString()),
                    Context.pop(trace)
            );
        }

        private Map<String, String> queries(String queryString) {
            if (queryString == null) return Collections.emptyMap();
            return Splitter.on('&').withKeyValueSeparator('=').split(queryString);
        }

        private Map<String, String> headers(final HttpServletRequest request) {
            return Maps.toMap(Collections.list(request.getHeaderNames()), new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return request.getHeader(input);
                }
            });
        }
    }

}
