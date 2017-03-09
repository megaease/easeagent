package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.utility.JavaModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

@AutoService(Plugin.class)
public class MetricServlet extends Transformation<MetricServlet.NoConfiguration> {

    @Override
    protected Feature feature(final NoConfiguration conf) {
        return new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                return isSubTypeOf(HttpServlet.class);
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                final String key = UUID.randomUUID().toString();
                return new AgentBuilder.Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader cld, JavaModule m) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(ForwardDetection.Key.class, key)
                                             .to(MarkAdvice.class)
                                             .on(takesArguments(HttpServletRequest.class, HttpServletResponse.class)));
                    }
                };
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
