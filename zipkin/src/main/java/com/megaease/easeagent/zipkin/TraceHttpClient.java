package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.Tracer;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Injector;
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
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import static brave.propagation.Propagation.B3_STRING;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class TraceHttpClient implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.apache.http.impl.client.CloseableHttpClient")))
                  .transform(execute(ElementMatchers.<MethodDescription>named("doExecute")))
                  .end();
    }

    @AdviceTo(Execute.class)
    abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);

    static class Execute {
        static final Injector<HttpRequest> INJECTOR = B3_STRING.injector(new Propagation.Setter<HttpRequest, String>() {
            @Override
            public void put(HttpRequest carrier, String key, String value) {
                carrier.addHeader(key, value);
            }
        });

        final ForwardLock lock;
        final CallTrace trace;
        final Tracer tracer;

        @Injection.Autowire
        Execute(CallTrace trace, Tracer tracer) {
            this.lock = new ForwardLock();
            this.trace = trace;
            this.tracer = tracer;
        }

        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.Argument(1) HttpRequest request) {
            if (!lock.acquire(method) || trace.peek() == null) return;

            final TraceContext context = trace.peek().<Span>context().context();
            trace.push(tracer.newChild(context).start());
            INJECTOR.inject(context, request);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Origin String method, @Advice.Argument(0) HttpHost host, @Advice.Argument(1) HttpRequest req,
                  @Advice.Return HttpResponse res, @Advice.Thrown Throwable error) {
            if (!lock.release(method) || trace.peek() == null) return;

            final int statusCode = res.getStatusLine().getStatusCode();
            trace.pop().<Span>context()
                    .name("http_send")
                    .kind(Span.Kind.CLIENT)
                    .tag("component", "apache-http-client")
                    .tag("span.kind", "client")
                    .tag("http.url", req.getRequestLine().getUri())
                    .tag("http.method", req.getRequestLine().getMethod())
                    // An error means request did not send out.
                    .tag("http.status_code", error == null ? String.valueOf(statusCode) : "999")
                    .tag("remote.address", host.getHostName() + (host.getPort() == -1 ? "" : ":" + host.getPort()))
                    .tag("has.error", error == null ? String.valueOf(statusCode >= 400) : "true")
                    .finish();

        }
    }
}
