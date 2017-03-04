package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hexdecteam.easeagent.TraceContext.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class OpentracingRestTemplate extends Transformation<OpentracingRestTemplate.NoConfiguration> {

    @Override
    protected Feature feature(NoConfiguration conf) {
        final String key = UUID.randomUUID().toString();
        return new Feature() {
            @Override
            public ElementMatcher.Junction<TypeDescription> type() {
                return isSubTypeOf(AbstractClientHttpRequest.class);
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new AgentBuilder.Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                        return builder.visit(Advice.withCustomMapping()
                                                   .bind(ForwardDetection.Key.class, key)
                                                   .to(TraceAdvice.class)
                                                   .on(named("executeInternal").and(returns(ClientHttpResponse.class))));

                    }
                };
            }
        };
    }

    interface NoConfiguration {}

    static class TraceAdvice {
        @Advice.OnMethodEnter
        public static boolean enter(@ForwardDetection.Key String key, @Advice.This ClientHttpRequest request) {
            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);

            if (!marked) return marked;

            final Tracer.SpanBuilder builder = tracer().buildSpan("http_send");
            final Span parent = TraceContext.peek();
            final Span span = (parent == null ? builder : builder.asChildOf(parent)).start();

            final HttpHeaders headers = request.getHeaders();
            final Map<String, String> ctx = new HashMap<String, String>();

            tracer().inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(ctx));

            for (Map.Entry<String, String> entry : ctx.entrySet()) {
                headers.add(entry.getKey(), entry.getValue());
            }

            span.log(ctx);

            push(span);

            return marked;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key,
                                @Advice.Enter boolean marked,
                                @Advice.This ClientHttpRequest request,
                                @Advice.Return ClientHttpResponse response) {
            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            try {
                pop().setTag("component", "spring-rest-template")
                     .setTag("span.kind", "client")
                     .setTag("http.url", request.getURI().toString())
                     .setTag("http.method", request.getMethod().toString())
                     .setTag("http.status_code", response.getRawStatusCode())
                     .finish();
            } catch (IOException ignore) {
                // never be here
            }
        }
    }
}
