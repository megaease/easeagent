package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.Tracer;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static brave.propagation.Propagation.B3_STRING;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class TraceRestTemplate implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.springframework.http.client.AbstractClientHttpRequest")))
                  .transform(execute(ElementMatchers.<MethodDescription>named("executeInternal")))
                  .end();
    }

    @AdviceTo(Execute.class)
    abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);


    static class Execute {
        static final TraceContext.Injector<HttpHeaders> INJECTOR = B3_STRING.injector(new Propagation.Setter<HttpHeaders, String>() {
            @Override
            public void put(HttpHeaders carrier, String key, String value) {
                carrier.add(key, value);
            }
        });


        final CallTrace trace;
        final Tracer tracer;
        final ForwardLock lock;
        final Logger logger;

        @Injection.Autowire
        Execute(CallTrace trace, Tracer tracer) {
            this.trace = trace;
            this.tracer = tracer;
            this.lock = new ForwardLock();
            logger = LoggerFactory.getLogger(getClass());
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Void> enter(@Advice.Argument(0) final HttpHeaders headers) {
            return lock.acquire(new ForwardLock.Supplier<Void>() {
                @Override
                public Void get() {
                    if (trace.peek() != null) {
                        final Span span = tracer.newChild(trace.peek().<Span>context().context()).start();
                        INJECTOR.inject(span.context(), headers);
                        trace.push(span);
                    }

                    return null;
                }
            });

        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Void> release, @Advice.This final ClientHttpRequest req,
                  @Advice.Return final ClientHttpResponse res, @Advice.Thrown final Throwable error) {

            release.apply(new ForwardLock.Consumer<Void>() {
                @Override
                public void accept(Void aVoid) {
                    if (trace.peek() == null) return;

                    final URI uri = req.getURI();
                    try {
                        trace.pop().<Span>context()
                                .name("http_send")
                                .kind(Span.Kind.CLIENT)
                                .tag("component", "spring-rest-template")
                                .tag("span.kind", "client")
                                .tag("http.url", uri.toString())
                                .tag("http.method", req.getMethod().toString())
                                .tag("http.status_code", error == null ? String.valueOf(res.getRawStatusCode()) : "999")
                                .tag("has.error", error == null ? String.valueOf(res.getRawStatusCode() >= 400) : "true")
                                .tag("remote.address", uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort()))
                                .finish();
                    } catch (IOException e) {
                        logger.error("Unexpected", e);
                    }
                }
            });
        }

    }
}
