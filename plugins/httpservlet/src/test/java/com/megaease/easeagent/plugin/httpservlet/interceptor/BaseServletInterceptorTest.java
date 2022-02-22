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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class BaseServletInterceptorTest {

    @Test
    public void doBefore() {
        HttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();

        MockBaseServletInterceptor mockBaseServletInterceptor = new MockBaseServletInterceptor();
        mockBaseServletInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        assertNotNull(httpServletRequest.getAttribute(ServletUtils.START_TIME));
    }

    @Test
    public void doAfter() {
        internalAfter(null);
    }

    @Test
    public void getAfterMark() {
        MockBaseServletInterceptor mockBaseServletInterceptor = new MockBaseServletInterceptor();
        assertEquals(MockBaseServletInterceptor.BEFORE_MARK, mockBaseServletInterceptor.getAfterMark());
    }

    @Test
    public void internalAfter() {
        internalAfter(new RuntimeException("test error"));
    }

    private void internalAfter(Throwable error) {
        HttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).throwable(error).build();

        MockBaseServletInterceptor mockBaseServletInterceptor = new MockBaseServletInterceptor();
        mockBaseServletInterceptor.throwable = error;
        mockBaseServletInterceptor.key = TestConst.METHOD + " " + TestConst.ROUTE;
        mockBaseServletInterceptor.httpServletRequest = httpServletRequest;
        mockBaseServletInterceptor.httpServletResponse = response;
        mockBaseServletInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        mockBaseServletInterceptor.start = (long) httpServletRequest.getAttribute(ServletUtils.START_TIME);
        mockBaseServletInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertTrue(mockBaseServletInterceptor.isRan.get());
    }


    @Test
    public void testAsync() throws InterruptedException {
        runAsyncOne(AsyncContext::complete, null);

        String errorInfo = "timeout eror";
        RuntimeException error = new RuntimeException(errorInfo);
        runAsyncOne(asyncContext -> {
            AsyncEvent asyncEvent = new AsyncEvent(asyncContext, error);
            MockAsyncContext mockAsyncContext = (MockAsyncContext) asyncContext;
            for (AsyncListener asyncListener : mockAsyncContext.getListeners()) {
                try {
                    asyncListener.onComplete(asyncEvent);
                } catch (IOException e) {
                    throw new RuntimeException("error");
                }
            }
        }, error);
    }

    private void runAsyncOne(Consumer<AsyncContext> asyncContextConsumer, Throwable error) throws InterruptedException {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        MockBaseServletInterceptor mockBaseServletInterceptor = new MockBaseServletInterceptor();
        mockBaseServletInterceptor.key = TestConst.METHOD + " " + TestConst.ROUTE;
        mockBaseServletInterceptor.httpServletRequest = httpServletRequest;
        mockBaseServletInterceptor.httpServletResponse = response;
        mockBaseServletInterceptor.throwable = error;

        mockBaseServletInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        mockBaseServletInterceptor.start = (long) httpServletRequest.getAttribute(ServletUtils.START_TIME);
        httpServletRequest.setAsyncSupported(true);
        final AsyncContext asyncContext = httpServletRequest.startAsync(httpServletRequest, response);
        mockBaseServletInterceptor.doAfter(methodInfo, EaseAgent.getContext());

        Thread thread = new Thread(() -> asyncContextConsumer.accept(asyncContext));
        thread.start();
        thread.join();
        assertTrue(mockBaseServletInterceptor.isRan.get());
    }

    private static class MockBaseServletInterceptor extends BaseServletInterceptor {
        private static final String BEFORE_MARK = MockBaseServletInterceptor.class.getName() + "$BeforeMark";
        private AtomicBoolean isRan = new AtomicBoolean(false);
        private Throwable throwable;
        private String key;
        private HttpServletRequest httpServletRequest;
        private HttpServletResponse httpServletResponse;
        private long start;


        @Override
        String getAfterMark() {
            return BEFORE_MARK;
        }

        @Override
        void internalAfter(Throwable throwable, String key, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long start) {
            isRan.set(true);
            if (this.throwable == null) {
                assertNull(throwable);
            } else {
                assertSame(this.throwable, throwable);
            }
            assertEquals(this.key, key);
            assertSame(this.httpServletRequest, httpServletRequest);
            assertSame(this.httpServletResponse, httpServletResponse);
            assertEquals(this.start, start);
        }
    }
}
