package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public interface Trace {
    Junction<TypeDescription> SERVLET = isSubTypeOf(Servlet.class);

    @InjectClass(StackFrame.class)
    @Configurable(prefix = "trace")
    class Transformation extends AbstractTransformation {
        List<String> packageIncludes = Collections.emptyList();
        List<String> packageExcludes = Collections.emptyList();

        @Override
        protected ElementMatcher<? super TypeDescription> ignores() {
            return startsWith(this.packageExcludes, none());
        }

        @Override
        protected AgentBuilder transformWith(AgentBuilder builder) {
            return builder
                    .type(SERVLET)
                    .transform((b, td, cl) -> b.visit(Advice.to(ServletRoot.class).on(named("service"))))
                    .type(not(SERVLET).and(startsWith(packageIncludes, any())))
                    .transform((b, td, cl) -> b.visit(Advice.to(Branches.class).on(methods())))
                    ;
        }

        private Junction<NamedElement> startsWith(List<String> strings, Junction<NamedElement> defaultValue) {
            return strings.stream()
                          .map(ElementMatchers::nameStartsWith)
                          .reduce(Junction::or)
                          .orElse(defaultValue);
        }


        private Junction<MethodDescription.InDefinedShape> methods() {
            return isPublic()
                    .and(not(isAbstract()))
                    .and(not(isFinalizer()))
                    .and(not(isHashCode()))
                    .and(not(isEquals()))
                    .and(not(isToString()))
                    .and(not(isClone()))
                    .and(not(isConstructor()))
                    .and(not(isTypeInitializer()))
                    .and(not(isSetter()))
                    .and(not(isGetter()))
                    ;
        }
    }

    class Branches {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Origin("#t::#m") String method) {
            StackFrame.fork(method);
        }

        @Advice.OnMethodExit()
        public static void exit() {
            StackFrame.join();
        }
    }

    class ServletRoot {
        @Advice.OnMethodEnter
        public static boolean enter(@Advice.Argument(0) ServletRequest request) {
            if (!(request instanceof HttpServletRequest)) return false;

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final String method = httpServletRequest.getMethod();
            final String requestURI = httpServletRequest.getRequestURI();
            return StackFrame.setIfAbsent(method + " " + requestURI);
        }

        @Advice.OnMethodExit()
        public static void exit(@Advice.Enter boolean set) {
            if (set) StackFrame.print();
        }

    }
}
