package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.utility.JavaModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Enumeration;

import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

@AutoService(Plugin.class)
public class TraceServlet extends Transformation<TraceServlet.Configuration> {

    @Override
    protected Feature feature(final Configuration conf) {
        final String regex = conf.tracing_header_regex();
        if (Strings.isNullOrEmpty(regex)) return Feature.NO_OP;
        return new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                return isSubTypeOf(HttpServlet.class);
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new AgentBuilder.Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader cld, JavaModule m) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(EnableTraceHeader.class, regex)
                                             .to(TracingAdvice.class)
                                             .on(takesArguments(HttpServletRequest.class, HttpServletResponse.class)));
                    }
                };
            }
        };
    }

    @ConfigurationDecorator.Binding("trace.servlet")
    static abstract class Configuration {
        /**
         * Trace request if a HTTP header matches the regex.
         *
         * @return empty as default
         */
        String tracing_header_regex() { return "";}

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface EnableTraceHeader {}

    static class TracingAdvice {

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        public static boolean enter(@Advice.Argument(0) HttpServletRequest request,
                                    @Advice.Origin String method,
                                    @EnableTraceHeader String regex) {

            final Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                final String name = names.nextElement();
                if (name.matches(regex)) {
                    return StackFrame.setRootIfAbsent(method);
                }
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Argument(0) HttpServletRequest request,
                                @Advice.Argument(1) HttpServletResponse response,
                                @Advice.Enter boolean forked) {

            if (forked) {
                final String uri = request.getRequestURI();
                final int status = response.getStatus();
                final StackFrame root = StackFrame.join();
                final String id = (String) request.getAttribute("stagemonitor-request-id");
                final String method = request.getMethod();
                EventBus.publish(new HTTPTracedRequest(id, root.getSignature(), root.getExecutionTime(),
                                                       root.getExecutionCPUTime(), root, status >= 400,
                                                       uri, method, status));
            }
        }
    }

}
