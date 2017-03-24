package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;

import static com.hexdecteam.easeagent.TraceContext.*;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

@AutoService(Plugin.class)
public class OpentracingJedis extends Transformation<Plugin.Noop> {
    @Override
    protected Feature feature(Noop conf) {
        return new Feature() {
            @Override
            public ElementMatcher.Junction<TypeDescription> type() {
                return named("redis.clients.jedis.Connection");
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new ForAdvice().include(getClass().getClassLoader())
                                      .advice(named("sendCommand").and(takesArgument(1, byte[][].class)),
                                              "com.hexdecteam.easeagent.OpentracingJedis$ProbeSend")
                                      .advice(named("readProtocolWithCheckingBroken"),
                                              "com.hexdecteam.easeagent.OpentracingJedis$ProbeRead");
            }
        };
    }

    static class ProbeSend {
        @Advice.OnMethodEnter
        static void enter(@Advice.Argument(0) Protocol.Command command, @Advice.This Connection conn) {
            final Tracer.SpanBuilder builder = tracer().buildSpan("redis_command")
                                                       .withTag("component", "jedis")
                                                       .withTag("span.kind", "client")
                                                       .withTag("redis.host", conn.getHost())
                                                       .withTag("redis.port", conn.getPort())
                                                       .withTag("redis.cmd", command.name());
            final Span parent = TraceContext.peek();
            push((parent == null ? builder : builder.asChildOf(parent)).start().log("cs"));
        }
    }

    static class ProbeRead {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(@Advice.Thrown Throwable error) {
            pop().log("cr").setTag("redis.result", error == null).finish();
        }
    }
}
