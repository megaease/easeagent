package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Enumeration;

import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class TraceServlet extends Transformation<TraceServlet.Configuration> {

    @Override
    protected Feature feature(final Configuration conf) {
        final String regex = conf.tracing_header_regex();
        if (Strings.isNullOrEmpty(regex)) return Feature.NO_OP;
        return new Feature() {

            @Override
            public Junction<TypeDescription> type() {
                return hasSuperType(named("javax.servlet.http.HttpServlet"));
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                final Junction<MethodDescription> method = takesArgument(0, named("javax.servlet.http.HttpServletRequest"))
                        .and(takesArgument(1, named("javax.servlet.http.HttpServletResponse")));
                return new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bind(EnableTraceHeader.class, regex))
                        .include(getClass().getClassLoader())
                        .advice(method, "com.hexdecteam.easeagent.TraceServlet$TracingAdvice");
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
                                    @Advice.Origin("#t\\##m") String method,
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
