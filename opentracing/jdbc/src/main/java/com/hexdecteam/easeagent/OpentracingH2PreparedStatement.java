package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Field;
import java.sql.Statement;

import static com.hexdecteam.easeagent.TraceContext.*;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

// TODO Clean code to share one plugin
@AutoService(Plugin.class)
public class OpentracingH2PreparedStatement extends Transformation<Plugin.Noop> {
    @Override
    protected Feature feature(Noop conf) {
        return new Feature() {
            @Override
            public ElementMatcher.Junction<TypeDescription> type() {
                return named("org.h2.jdbc.JdbcPreparedStatement");
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new ForAdvice()
                        .include(getClass().getClassLoader())
                        .advice(nameStartsWith("execute"), "com.hexdecteam.easeagent.OpentracingH2PreparedStatement$Probe");
            }
        };
    }

    static class Probe {
        // TODO Clean code to share one entering class.
        @Advice.OnMethodEnter
        public static void enter() {
            final Tracer.SpanBuilder builder = tracer().buildSpan("jdbc_statement");
            final Span parent = TraceContext.peek();
            push((parent == null ? builder : builder.asChildOf(parent)).start().log("cs"));
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.This Statement stmt, @Advice.Thrown Throwable error) {
            try {
                final Field field = stmt.getClass().getDeclaredField("sqlStatement");
                field.setAccessible(true);
                final String sql = field.get(stmt).toString();

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
