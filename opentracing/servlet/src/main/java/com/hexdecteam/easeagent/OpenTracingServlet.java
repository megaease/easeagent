package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.propagation.TextMapExtractAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hexdecteam.easeagent.TraceContext.*;
import static io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class OpenTracingServlet extends Transformation<Plugin.Noop> {

    @Override
    protected Feature feature(Noop conf) {
        return new Feature() {
            final String key = UUID.randomUUID().toString();

            @Override
            public Junction<TypeDescription> type() {
                return hasSuperType(named("javax.servlet.http.HttpServlet"));
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                final Junction<MethodDescription> method = takesArgument(0, named("javax.servlet.http.HttpServletRequest"))
                        .and(takesArgument(1, named("javax.servlet.http.HttpServletResponse")));
                return new ForAdvice(Advice.withCustomMapping().bind(ForwardDetection.Key.class, key))
                        .include(getClass().getClassLoader())
                        .advice(method, "com.hexdecteam.easeagent.OpenTracingServlet$TraceAdvice");
            }
        };
    }


    static class TraceAdvice {
        @Advice.OnMethodEnter
        public static boolean enter(@ForwardDetection.Key String key,
                                    @Advice.Argument(0) HttpServletRequest request) {
            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);

            if (!marked) return marked;

            final Map<String, String> headers = new HashMap<String, String>();

            final Enumeration<String> names = request.getHeaderNames();

            while (names.hasMoreElements()) {
                final String k = names.nextElement();

                // This ugly hard code is used to fix the lower case header name bug in tomcat.
                if (k.equalsIgnoreCase("X-B3-TraceId")) {
                    headers.put("X-B3-TraceId", request.getHeader(k));
                } else if (k.equalsIgnoreCase("X-B3-SpanId")) {
                    headers.put("X-B3-SpanId", request.getHeader(k));
                } else if (k.equalsIgnoreCase("X-B3-ParentSpanId")) {
                    headers.put("X-B3-ParentSpanId", request.getHeader(k));
                } else if (k.equalsIgnoreCase("X-B3-Sampled")) {
                    headers.put("X-B3-Sampled", request.getHeader(k));
                } else if (k.equalsIgnoreCase("X-B3-Flags")) {
                    headers.put("X-B3-Flags", request.getHeader(k));
                }
            }

            final Span span = tracer().buildSpan("http_recv")
                                      .asChildOf(tracer().extract(HTTP_HEADERS, new TextMapExtractAdapter(headers)))
                                      .start();
            span.log("sr");
            push(span);

            return marked;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key,
                                @Advice.Argument(0) HttpServletRequest request,
                                @Advice.Argument(1) HttpServletResponse response,
                                @Advice.Enter boolean marked) {
            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            final StringBuffer requestURL = request.getRequestURL();
            final String query = request.getQueryString();
            final String url = query == null ? requestURL.toString() : requestURL.append('?').append(query).toString();

            pop().log("ss")
                 .setTag("component", "servlet")
                 .setTag("span.kind", "server")
                 .setTag("http.url", url)
                 .setTag("http.method", request.getMethod())
                 .setTag("http.status_code", response.getStatus())
                 .setTag("peer.hostname", request.getRemoteHost())
                 .setTag("peer.ipv4", request.getRemoteAddr())
                 .setTag("peer.port", request.getRemotePort())
                 .finish();
        }
    }
}
