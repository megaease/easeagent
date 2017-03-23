package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hexdecteam.easeagent.TraceContext.*;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@AutoService(Plugin.class)
public class OpenTracingHttpClient extends Transformation<Plugin.Noop> {
    @Override
    protected Feature feature(Noop conf) {
        final String key = UUID.randomUUID().toString();
        return new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                return hasSuperType(named("org.apache.http.impl.client.CloseableHttpClient"));
            }

            @Override
            public AgentBuilder.Transformer transformer() {

                return new ForAdvice(Advice.withCustomMapping().bind(ForwardDetection.Key.class, key))
                        .include(getClass().getClassLoader())
                        .advice(named("doExecute"), "com.hexdecteam.easeagent.OpenTracingHttpClient$Probe");
            }
        };
    }

    static class Probe {
        @Advice.OnMethodEnter
        public static boolean enter(@ForwardDetection.Key String key, @Advice.Argument(1) HttpRequest request) {

            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);

            if (!marked) return marked;

            try {
                final Tracer.SpanBuilder builder = tracer().buildSpan("http_send");
                final Span parent = TraceContext.peek();
                final Span span = (parent == null ? builder : builder.asChildOf(parent)).start();

                final Map<String, String> ctx = new HashMap<String, String>();

                tracer().inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(ctx));

                for (Map.Entry<String, String> entry : ctx.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }

                push(span.log("cs"));
            } catch (Exception ignore) { }

            return marked;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key,
                                @Advice.Enter boolean marked,
                                @Advice.Argument(1) HttpRequest request,
                                @Advice.Return HttpResponse response) {
            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            pop().log("cr")
                 .setTag("component", "apache-http-client")
                 .setTag("span.kind", "client")
                 .setTag("http.url", request.getRequestLine().getUri())
                 .setTag("http.method", request.getRequestLine().getMethod())
                 .setTag("http.status_code", Integer.toString(response.getStatusLine().getStatusCode()))
                 .finish();
        }


    }
}
