package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
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
            public Junction<TypeDescription> type() {
                return named("redis.clients.jedis.Connection");
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new ForAdvice().include(getClass().getClassLoader())
                                      .advice(named("sendCommand").and(ElementMatchers.<MethodDescription>isVarArgs()).and(takesArgument(1, byte[][].class)),
                                              "com.hexdecteam.easeagent.OpentracingJedis$ProbeSend")
                                      .advice(named("readProtocolWithCheckingBroken"),
                                              "com.hexdecteam.easeagent.OpentracingJedis$ProbeRead");
            }
        };
    }

    static class ProbeSend {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(@Advice.Argument(0) Protocol.Command command, @Advice.This Connection conn, @Advice.Thrown Throwable error) {
            final Tracer.SpanBuilder builder = tracer().buildSpan("redis_command")
                                                       .withTag("component", "jedis")
                                                       .withTag("span.kind", "client")
                                                       .withTag("redis.host", conn.getHost())
                                                       .withTag("redis.port", conn.getPort())
                                                       .withTag("redis.cmd", command.name())
                                                       .withTag("remote.address", conn.getHost() + ":" + conn.getPort());
            final Span parent = TraceContext.peek();
            push((parent == null ? builder : builder.asChildOf(parent)).start().log("cs"));

            if (error != null) pop().log("cr").setTag("redis.result", false).finish();
        }

    }

    static class ProbeRead {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(@Advice.Thrown Throwable error) {
            pop().log("cr")
                 .setTag("redis.result", error == null)
                 .setTag("has.error", error != null)
                 .finish();
        }
    }
}
