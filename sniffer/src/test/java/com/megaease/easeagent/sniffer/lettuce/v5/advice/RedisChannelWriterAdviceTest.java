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

package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import io.lettuce.core.RedisChannelWriter;
import io.lettuce.core.protocol.ConnectionFacade;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.ClientResources;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

public class RedisChannelWriterAdviceTest extends BaseSnifferTest {
    static List<Class<?>> classList;
    AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
    AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
    Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;

    @Before
    public void before() throws Exception {
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenRedisChannelWriterAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyRedisChannelWriter"
        )
                .with(def,
                        new QualifiedBean("supplier4LettuceDoWrite", supplier),
                        new QualifiedBean("", chainInvoker))
                .load(loader);

    }


    @Test
    public void writeSuccess() throws Exception {
        MyRedisChannelWriter channelWriter = (MyRedisChannelWriter) classList.get(0).newInstance();
        RedisCommand redisCommand = mock(RedisCommand.class);
        channelWriter.write(redisCommand);
        this.verifyInvokeTimes(chainInvoker, 1);

    }

    static class MyRedisChannelWriter implements RedisChannelWriter {

        @Override
        public <K, V, T> RedisCommand<K, V, T> write(RedisCommand<K, V, T> command) {
            return null;
        }

        @Override
        public <K, V> Collection<RedisCommand<K, V, ?>> write(Collection<? extends RedisCommand<K, V, ?>> redisCommands) {
            return null;
        }

        @Override
        public void close() {

        }

        @Override
        public CompletableFuture<Void> closeAsync() {
            return null;
        }

        @Override
        public void reset() {

        }

        @Override
        public void setConnectionFacade(ConnectionFacade connection) {

        }

        @Override
        public void setAutoFlushCommands(boolean autoFlush) {

        }

        @Override
        public void flushCommands() {

        }

        @Override
        public ClientResources getClientResources() {
            return null;
        }
    }

}
