/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.context;

import com.megaease.easeagent.config.PluginConfig;
import com.megaease.easeagent.config.PluginConfigManager;
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.bridge.NoOpIPluginConfig;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.megaease.easeagent.plugin.api.ProgressFields.EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG;
import static org.junit.Assert.*;

public class SessionContextTest {
    ContextManager contextManager;

    @Before
    public void before() {
        ContextManager contextManager = ContextManager.build(ConfigMock.getCONFIGS());
        assertNotNull(contextManager);
    }

    @Test
    public void isNoop() {
        SessionContext sessionContext = new SessionContext();
        assertFalse(sessionContext.isNoop());
    }

    @Test
    public void getSupplier() {
        SessionContext sessionContext = new SessionContext();
        assertNull(sessionContext.getSupplier());
        sessionContext.setSupplier(() -> null);
        assertNotNull(sessionContext.getSupplier());
    }

    @Test
    public void setSupplier() {
        getSupplier();
    }

    @Test
    public void currentTracing() {
        SessionContext sessionContext = new SessionContext();
        assertTrue(sessionContext.currentTracing().isNoop());
        sessionContext.setCurrentTracing(new MockITracing());
        assertNotNull(sessionContext.currentTracing());
        assertFalse(sessionContext.currentTracing().isNoop());
    }

    @Test
    public void get() {
        String name = "test_name";
        String value = "test_value";
        SessionContext sessionContext = new SessionContext();
        assertNull(sessionContext.get(name));
        sessionContext.put(name, value);
        assertEquals(value, sessionContext.get(name));
        sessionContext.remove(name);
        assertNull(sessionContext.get(name));
    }

    @Test
    public void remove() {
        get();
    }

    @Test
    public void put() {
        get();
    }

    @Test
    public void getLocal() {
        //Deprecated
    }

    @Test
    public void putLocal() {
        //Deprecated
    }


    @Test
    public void getConfig() {
        SessionContext sessionContext = new SessionContext();
        assertNotNull(sessionContext.getConfig());
        IPluginConfig iPluginConfig = sessionContext.getConfig();
        assertEquals(NoOpIPluginConfig.INSTANCE.domain(), iPluginConfig.domain());
        assertEquals(NoOpIPluginConfig.INSTANCE.namespace(), iPluginConfig.namespace());
        assertEquals(NoOpIPluginConfig.INSTANCE.id(), iPluginConfig.id());

        sessionContext.pushConfig(NoOpIPluginConfig.INSTANCE);
        assertNotNull(sessionContext.getConfig());
        PluginConfigManager pluginConfigManager = PluginConfigManager.builder(ConfigMock.getCONFIGS()).build();
        String domain = "observability";
        String namespace = "test_config";
        String id = "test";
        PluginConfig config = pluginConfigManager.getConfig(domain, namespace, id);
        sessionContext.pushConfig(config);

        iPluginConfig = sessionContext.getConfig();
        assertEquals(domain, iPluginConfig.domain());
        assertEquals(namespace, iPluginConfig.namespace());
        assertEquals(id, iPluginConfig.id());

        IPluginConfig oppConfig = sessionContext.popConfig();
        assertEquals(domain, oppConfig.domain());
        assertEquals(namespace, oppConfig.namespace());
        assertEquals(id, oppConfig.id());

        iPluginConfig = sessionContext.getConfig();
        assertEquals(NoOpIPluginConfig.INSTANCE.domain(), iPluginConfig.domain());
        assertEquals(NoOpIPluginConfig.INSTANCE.namespace(), iPluginConfig.namespace());
        assertEquals(NoOpIPluginConfig.INSTANCE.id(), iPluginConfig.id());

    }

    @Test
    public void pushConfig() {
        getConfig();
    }

    @Test
    public void popConfig() {
        getConfig();
    }

    @Test
    public void enter() {
        SessionContext sessionContext = new SessionContext();
        Object key1 = new Object();
        Object key2 = new Object();
        assertEquals(1, sessionContext.enter(key1));
        assertEquals(1, sessionContext.enter(key2));
        assertEquals(2, sessionContext.enter(key1));
        assertEquals(2, sessionContext.enter(key2));
        assertEquals(2, sessionContext.exit(key1));
        assertEquals(2, sessionContext.exit(key2));
        assertEquals(1, sessionContext.exit(key1));
        assertEquals(1, sessionContext.exit(key2));


        assertTrue(sessionContext.enter(key1, 1));
        assertTrue(sessionContext.enter(key2, 1));
        assertTrue(sessionContext.enter(key1, 2));
        assertTrue(sessionContext.enter(key2, 2));
        assertTrue(sessionContext.exit(key1, 2));
        assertTrue(sessionContext.exit(key2, 2));
        assertTrue(sessionContext.exit(key1, 1));
        assertTrue(sessionContext.exit(key2, 1));

    }

    @Test
    public void exit() {
        enter();
    }

    @Test
    public void exportAsync() throws InterruptedException {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setSupplier(EaseAgent.initializeContextSupplier);
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        String name = "test_name";
        String value = "test_value";
        assertNull(sessionContext.get(name));
        sessionContext.put(name, value);
        assertEquals(value, sessionContext.get(name));
        AsyncContext asyncContext = sessionContext.exportAsync();
        assertTrue(asyncContext.getSpanContext().isNoop());
        assertEquals(1, iTracing.exportAsyncCount.get());

        assertEquals(value, asyncContext.get(name));
        assertFalse(asyncContext.isNoop());
        SessionContext sessionContext2 = new SessionContext();
        sessionContext2.setCurrentTracing(iTracing);
        assertNull(sessionContext2.get(name));
        try (Cleaner cleaner = sessionContext2.importAsync(asyncContext)) {
            assertEquals(value, sessionContext2.get(name));
            assertEquals(1, iTracing.importAsyncCount.get());
            try (Cleaner cleaner2 = sessionContext2.importAsync(asyncContext)) {
                assertEquals(2, iTracing.importAsyncCount.get());
            }
            assertEquals(value, sessionContext2.get(name));
        }

    }

    @Test
    public void importAsync() throws InterruptedException {
        exportAsync();
    }

    @Test
    public void clientRequest() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.clientRequest(new EmptyRequest());
        assertEquals(1, iTracing.clientRequestCount.get());
    }

    @Test
    public void serverReceive() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.serverReceive(new EmptyRequest());
        assertEquals(1, iTracing.serverReceiveCount.get());

    }

    @Test
    public void consumerSpan() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.consumerSpan(new EmptyRequest());
        assertEquals(1, iTracing.consumerSpanCount.get());

    }

    @Test
    public void producerSpan() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.producerSpan(new EmptyRequest());
        assertEquals(1, iTracing.producerSpanCount.get());
    }

    @Test
    public void nextSpan() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.nextSpan();
        assertEquals(1, iTracing.nextSpanCount.get());

    }

    @Test
    public void popToBound() {
        //Deprecated
    }

    @Test
    public void pushRetBound() {
        //Deprecated
    }

    @Test
    public void popRetBound() {
        //Deprecated
    }

    @Test
    public void push() {
        //Deprecated
    }

    @Test
    public void pop() {
        //Deprecated
    }

    @Test
    public void peek() {
        //Deprecated
    }

    @Test
    public void wrap() throws InterruptedException {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        String name = "test_name";
        String value = "test_value";
        sessionContext.put(name, value);

        AtomicReference<SessionContext> lastSessionContext = new AtomicReference<>();
        Supplier<InitializeContext> newContextSupplier = () -> {
            SessionContext sessionContext1 = new SessionContext();
            sessionContext1.setCurrentTracing(iTracing);
            lastSessionContext.set(sessionContext1);
            return sessionContext1;
        };
        sessionContext.setSupplier(newContextSupplier);
        Runnable runnable = () -> {
            assertNotNull(lastSessionContext.get());
            assertEquals(value, lastSessionContext.get().get(name));

        };
        runnable = sessionContext.wrap(runnable);
        assertTrue(sessionContext.isWrapped(runnable));
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        assertNull(lastSessionContext.get().get(name));
        assertEquals(1, iTracing.exportAsyncCount.get());
        assertEquals(1, iTracing.importAsyncCount.get());
    }

    @Test
    public void isWrapped() throws InterruptedException {
        wrap();
    }

    @Test
    public void isNecessaryKeys() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        iTracing.setPropagationKeys(Arrays.asList(new String[]{"b3"}));
        sessionContext.setCurrentTracing(iTracing);
        assertTrue(sessionContext.isNecessaryKeys("b3"));
        assertFalse(sessionContext.isNecessaryKeys("aaa"));

    }

    @Test
    public void consumerInject() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.consumerInject(NoOpTracer.NO_OP_SPAN, new EmptyRequest());
        assertEquals(1, iTracing.mockMessagingTracing.consumerInjectorCount.get());

    }

    @Test
    public void producerInject() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        sessionContext.producerInject(NoOpTracer.NO_OP_SPAN, new EmptyRequest());
        assertEquals(1, iTracing.mockMessagingTracing.producerInjectorCount.get());

    }

    @Test
    public void injectForwardedHeaders() {
        String forwarded = "test_forwarded_value";
        String keyPrefix = EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG;
        String forwardedKey = keyPrefix + "test_forwarded_key";
        String headerValue = "test_value";
        ProgressFields.changeListener().accept(Collections.singletonMap(forwardedKey, forwarded));
        ProgressFields.changeListener().accept(Collections.singletonMap(forwardedKey + "2", forwarded + "2"));
        SessionContext sessionContext = new SessionContext();
        sessionContext.importForwardedHeaders(name -> {
            if (name.equals(forwarded)) {
                return headerValue;
            }
            return null;
        });


        Map<String, String> values = new HashMap<>();
        sessionContext.injectForwardedHeaders(values::put);
        assertEquals(1, values.size());
        assertEquals(headerValue, values.get(forwarded));

        ProgressFields.changeListener().accept(Collections.singletonMap(forwardedKey, ""));
        ProgressFields.changeListener().accept(Collections.singletonMap(forwardedKey + "2", ""));
    }

    @Test
    public void importForwardedHeaders() {
        injectForwardedHeaders();
    }

    @Test
    public void getTracing() {
        SessionContext sessionContext = new SessionContext();
        MockITracing iTracing = new MockITracing();
        sessionContext.setCurrentTracing(iTracing);
        assertNotNull(sessionContext.getTracing());
        assertTrue(sessionContext.getTracing() instanceof MockITracing);
    }

    @Test
    public void setCurrentTracing() {
        getTracing();
    }

    @Test
    public void clear() {
        String name = "test_name";
        String value = "test_value";
        SessionContext sessionContext = new SessionContext();
        sessionContext.put(name, value);
        assertEquals(1, sessionContext.enter("test_key"));
        sessionContext.clear();
        assertNull(sessionContext.get(name));
        assertEquals(1, sessionContext.enter("test_key"));
        sessionContext.clear();
    }


    public static class EmptyRequest implements MessagingRequest {

        @Override
        public String operation() {
            return null;
        }

        @Override
        public String channelKind() {
            return null;
        }

        @Override
        public String channelName() {
            return null;
        }

        @Override
        public Object unwrap() {
            return null;
        }

        @Override
        public Span.Kind kind() {
            return null;
        }

        @Override
        public String header(String name) {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {

        }
    }


    public static class MockITracing extends NoOpTracer.NoopTracing {
        public AtomicInteger exportAsyncCount = new AtomicInteger();
        public AtomicInteger importAsyncCount = new AtomicInteger();
        public AtomicInteger clientRequestCount = new AtomicInteger();
        public AtomicInteger serverReceiveCount = new AtomicInteger();
        public AtomicInteger consumerSpanCount = new AtomicInteger();
        public AtomicInteger producerSpanCount = new AtomicInteger();
        public AtomicInteger nextSpanCount = new AtomicInteger();
        public MockMessagingTracing mockMessagingTracing = new MockMessagingTracing();
        private List<String> propagationKeys = Collections.emptyList();

        public MockITracing setPropagationKeys(List<String> propagationKeys) {
            this.propagationKeys = propagationKeys;
            return this;
        }

        @Override
        public SpanContext exportAsync() {
            exportAsyncCount.incrementAndGet();
            return super.exportAsync();
        }

        @Override
        public Scope importAsync(SpanContext snapshot) {
            importAsyncCount.incrementAndGet();
            return super.importAsync(snapshot);
        }

        @Override
        public RequestContext clientRequest(Request request) {
            clientRequestCount.incrementAndGet();
            return super.clientRequest(request);
        }

        @Override
        public RequestContext serverReceive(Request request) {
            serverReceiveCount.incrementAndGet();
            return super.serverReceive(request);
        }

        @Override
        public Span consumerSpan(MessagingRequest request) {
            consumerSpanCount.incrementAndGet();
            return super.consumerSpan(request);
        }

        @Override
        public Span producerSpan(MessagingRequest request) {
            producerSpanCount.incrementAndGet();
            return super.producerSpan(request);
        }

        @Override
        public Span nextSpan() {
            nextSpanCount.incrementAndGet();
            return super.nextSpan();
        }

        @Override
        public List<String> propagationKeys() {
            return propagationKeys;
        }

        @Override
        public MessagingTracing<MessagingRequest> messagingTracing() {
            return mockMessagingTracing;
        }

        @Override
        public boolean isNoop() {
            return false;
        }
    }


    public static class MockMessagingTracing extends NoOpTracer.EmptyMessagingTracing {
        public AtomicInteger producerInjectorCount = new AtomicInteger();
        public AtomicInteger consumerInjectorCount = new AtomicInteger();

        @Override
        public Injector<MessagingRequest> producerInjector() {
            return (span, request) -> producerInjectorCount.incrementAndGet();
        }

        @Override
        public Injector<MessagingRequest> consumerInjector() {
            return (span, request) -> consumerInjectorCount.incrementAndGet();
        }
    }
}
