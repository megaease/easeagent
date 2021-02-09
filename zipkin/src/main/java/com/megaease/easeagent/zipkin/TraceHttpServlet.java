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

package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.Tracer;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static brave.propagation.Propagation.B3_STRING;

@Injection.Provider(Provider.class)
public abstract class TraceHttpServlet extends HttpServletService {
    @Override
    @AdviceTo(Service.class)
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

    static class Service {

        private static final Extractor<HttpServletRequest> EXTRACTOR = B3_STRING.extractor(new Propagation.Getter<HttpServletRequest, String>() {
            @Override
            public String get(HttpServletRequest carrier, String key) {
                final String header = carrier.getHeader(key);
                return header != null ? header
                        // fix the lower case header name bug in tomcat.
                        : carrier.getHeader(key.toLowerCase());
            }
        });

        private final ForwardLock lock;
        private final Tracer tracer;
        private final CallTrace trace;

        @Injection.Autowire
        Service(Tracer tracer, CallTrace trace) {
            this.lock = new ForwardLock();
            this.tracer = tracer;
            this.trace = trace;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Void> enter(@Advice.Argument(0) final HttpServletRequest request) {
            return lock.acquire(new ForwardLock.Supplier<Void>() {
                @Override
                public Void get() {
                    final TraceContextOrSamplingFlags result = EXTRACTOR.extract(request);

//                    final TraceContext context = result.context() == null
//                            ? tracer.newTrace(result.samplingFlags()).context()
//                            : result.context().toBuilder().build();

                    TraceContext context = result.context() == null
                            ? tracer.nextSpan(result).context()
                            : result.context().toBuilder().build();

                    trace.push(
                            tracer.newChild(context)
                                    // To fix bug of timestamp drift in distribution.
                                    // Honestly i don't know why it can fix, but it fixed.
                                    .tag("current.milliseconds", String.valueOf(System.currentTimeMillis()))
                                    .start()
                    );
                    return null;
                }
            });

        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Void> release, @Advice.Argument(0) final HttpServletRequest request,
                  @Advice.Argument(1) final HttpServletResponse response) {

            release.apply(new ForwardLock.Consumer<Void>() {
                @Override
                public void accept(Void aVoid) {
                    trace.pop().<Span>context()
                            .name("http_recv")
                            .kind(Span.Kind.SERVER)
                            .tag("component", "web")
                            .tag("span.kind", "server")
                            .tag("http.url", request.getRequestURL().toString())
                            .tag("http.method", request.getMethod())
                            .tag("http.status_code", String.valueOf(response.getStatus()))
                            .tag("peer.hostname", request.getRemoteHost())
                            .tag("peer.ipv4", request.getRemoteAddr())
                            .tag("peer.port", String.valueOf(request.getRemotePort()))
                            .tag("has.error", String.valueOf(response.getStatus() >= 400))
                            .tag("remote.address", request.getRemoteAddr())
                            .finish();
                }
            });

        }
    }
}
