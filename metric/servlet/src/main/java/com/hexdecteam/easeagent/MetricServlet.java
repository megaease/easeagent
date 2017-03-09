package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class MetricServlet extends Transformation<MetricServlet.NoConfiguration> {

    @Override
    protected Feature feature(final NoConfiguration conf) {
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
                return new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bind(ForwardDetection.Key.class, key))
                        .include(getClass().getClassLoader())
                        .advice(method, "com.hexdecteam.easeagent.MetricServlet$MarkAdvice");
            }
        };
    }

    interface NoConfiguration {}

    static class MarkAdvice {

        @Advice.OnMethodEnter
        public static boolean enter(@ForwardDetection.Key String key) {
            return ForwardDetection.Mark.markIfAbsent(key);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Argument(0) HttpServletRequest request,
                                @Advice.Argument(1) HttpServletResponse response,
                                @Advice.Origin String signature, @Advice.Enter boolean marked,
                                @ForwardDetection.Key String key) {
            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            final String uri = request.getRequestURI();
            final int status = response.getStatus();

            // TODO lazy calculation in streaming
            EventBus.publish(new MetricEvents.Mark("request_throughput").tag("request_name", signature).tag("url", uri));
            // TODO lazy calculation in streaming
            EventBus.publish(new MetricEvents.Mark("request_throughput").tag("request_name", "All").tag("url", "All"));

            final String http_code = Integer.toString(status);
            EventBus.publish(new MetricEvents.Mark("request_throughput").tag("request_name", signature)
                                                                        .tag("http_code", http_code)
                                                                        .tag("url", uri));
            // TODO lazy calculation in streaming
            EventBus.publish(new MetricEvents.Mark("request_throughput").tag("request_name", "All")
                                                                        .tag("http_code", http_code)
                                                                        .tag("url", "All"));
            if (status >= 400) {
                EventBus.publish(new MetricEvents.Mark("request_error_throughput").tag("request_name", signature)
                                                                                  .tag("url", uri));
                // TODO lazy calculation in streaming
                EventBus.publish(new MetricEvents.Mark("request_error_throughput").tag("request_name", "All")
                                                                                  .tag("url", "All"));
            }

        }
    }

}
