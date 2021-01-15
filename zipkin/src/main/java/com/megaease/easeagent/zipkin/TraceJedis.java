/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.megaease.easeagent.zipkin;

import brave.Span;
import brave.Tracer;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

@Injection.Provider(Provider.class)
public abstract class TraceJedis implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(ElementMatchers.<TypeDescription>named("redis.clients.jedis.Connection"))
                  .transform(read(ElementMatchers.<MethodDescription>named("readProtocolWithCheckingBroken")))
                  .transform(send(named("sendCommand").and(ElementMatchers.<MethodDescription>isVarArgs()).and(takesArgument(1, byte[][].class))))
                  .end();
    }

    @AdviceTo(Read.class)
    abstract Definition.Transformer read(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(Send.class)
    abstract Definition.Transformer send(ElementMatcher<? super MethodDescription> matcher);

    static class Send {
        final CallTrace trace;
        final Tracer tracer;

        @Injection.Autowire
        Send(CallTrace trace, Tracer tracer) {
            this.trace = trace;
            this.tracer = tracer;
        }

        @Advice.OnMethodEnter
        void enter(@Advice.Argument(0) Protocol.Command command, @Advice.This Connection conn) {
            if (trace.peek() == null) return;

            final Span span = tracer.newChild(trace.peek().<Span>context().context())
                                    .name("redis_command")
                                    .kind(Span.Kind.CLIENT)
                                    .tag("component", "jedis")
                                    .tag("span.kind", "client")
                                    .tag("redis.host", conn.getHost())
                                    .tag("redis.port", String.valueOf(conn.getPort()))
                                    .tag("redis.cmd", command.name())
                                    .tag("remote.address", HostAddress.address(conn.getHost()))
                                    .tag("remote.type", "redis")
                                    .start();

            trace.push(span);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Thrown Throwable error) {
            if (error == null || trace.peek() == null) return;

            trace.pop().<Span>context()
                    .tag("redis.result", String.valueOf(false))
                    .tag("has.error", String.valueOf(true))
                    .finish();
        }
    }

    static class Read {
        final CallTrace trace;

        @Injection.Autowire
        Read(CallTrace trace) {
            this.trace = trace;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Thrown Throwable error) {
            if (trace.peek() == null) return;

            trace.pop().<Span>context()
                    .tag("redis.result", String.valueOf(error == null))
                    .tag("has.error", String.valueOf(error != null))
                    .finish();

        }
    }
}
