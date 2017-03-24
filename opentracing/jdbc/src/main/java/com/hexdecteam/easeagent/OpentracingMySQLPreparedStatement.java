package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

import static com.hexdecteam.easeagent.TraceContext.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

// TODO Clean code to share one plugin
@AutoService(Plugin.class)
public class OpentracingMySQLPreparedStatement extends Transformation<Plugin.Noop> {
    @Override
    protected Feature feature(Noop conf) {
        final String key = UUID.randomUUID().toString();
        return new Feature() {
            @Override
            public ElementMatcher.Junction<TypeDescription> type() {
                return isSubTypeOf(PreparedStatement.class)
                        .and(declaresMethod(ElementMatchers.<MethodDescription>isPublic()
                                                    .and(named("asSql"))
                                                    .and(returns(String.class))
                                                    .and(takesArguments(0))));
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new ForAdvice(Advice.withCustomMapping().bind(ForwardDetection.Key.class, key))
                        .include(getClass().getClassLoader())
                        .advice(nameStartsWith("execute"), "com.hexdecteam.easeagent.OpentracingMySQLPreparedStatement$Probe");
            }
        };
    }


    static class Probe {
        // TODO Clean code to share one entering class.
        @Advice.OnMethodEnter
        public static boolean enter(@ForwardDetection.Key String key) {
            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);
            // A union value for both marked and forked with bit operation.
            if (!marked) return marked;

            final Tracer.SpanBuilder builder = tracer().buildSpan("jdbc_statement");
            final Span parent = TraceContext.peek();
            push((parent == null ? builder : builder.asChildOf(parent)).start().log("cs"));

            return marked;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(
                @ForwardDetection.Key String key, @Advice.Enter boolean marked,
                @Advice.This Statement stmt, @Advice.Thrown Throwable error
        ) {
            if (!marked) return;

            ForwardDetection.Mark.clear(key);

            try {
                final Method method = stmt.getClass().getMethod("asSql");
                final String sql = method.invoke(stmt).toString();
                pop().log("cr")
                     .setTag("component", "jdbc")
                     .setTag("span.kind", "client")
                     .setTag("jdbc.url", stmt.getConnection().getMetaData().getURL())
                     .setTag("jdbc.sql", sql)
                     .setTag("jdbc.result", error == null)
                     .finish();
            } catch (Exception ignore) { }

        }

    }

}
