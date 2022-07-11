/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Setter;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.tools.trace.TraceConst;
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
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class DoFilterTraceInterceptorTest {

    private void testTrace() {
        HttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();

        DoFilterTraceInterceptor doFilterTraceInterceptor = new DoFilterTraceInterceptor();
        doFilterTraceInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        Object o = httpServletRequest.getAttribute(ServletUtils.PROGRESS_CONTEXT);
        assertNotNull(o);
        assertTrue(o instanceof RequestContext);
        doFilterTraceInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        Object o2 = httpServletRequest.getAttribute(ServletUtils.PROGRESS_CONTEXT);
        assertNotNull(o2);
        assertSame(o, o2);
        doFilterTraceInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertNull(mockSpan.parentId());
        checkServerSpan(mockSpan);

        MockEaseAgent.cleanLastSpan();
        doFilterTraceInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(MockEaseAgent.getLastSpan());
    }


    private void checkServerSpan(ReportSpan mockSpan) {
        assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
        assertEquals(TestConst.ROUTE, mockSpan.tag(TraceConst.HTTP_ATTRIBUTE_ROUTE));
        assertEquals(TestConst.METHOD, mockSpan.tag("http.method"));
        assertEquals(TestConst.URL, mockSpan.tag("http.path"));
        assertEquals(TestConst.REMOTE_ADDR, mockSpan.remoteEndpoint().ipv4());
        assertEquals(TestConst.REMOTE_PORT, mockSpan.remoteEndpoint().port());
    }

    @Test
    public void testErrorTracing() {
        HttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        String errorInfo = "test error";
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).throwable(new RuntimeException(errorInfo)).build();
        DoFilterTraceInterceptor doFilterTraceInterceptor = new DoFilterTraceInterceptor();
        doFilterTraceInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        doFilterTraceInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
        assertNull(mockSpan.parentId());
        checkServerSpan(mockSpan);
        assertEquals(errorInfo, mockSpan.tag("error"));
    }

    @Test
    public void testHasPassHeader() {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        Context context = EaseAgent.getContext();
        RequestContext requestContext = context.clientRequest(new MockClientRequest(httpServletRequest::addHeader));
        requestContext.scope().close();
        assertFalse(context.currentTracing().hasCurrentSpan());

        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        DoFilterTraceInterceptor doFilterTraceInterceptor = new DoFilterTraceInterceptor();
        doFilterTraceInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        doFilterTraceInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(requestContext.span().traceIdString(), mockSpan.traceId());
        assertEquals(requestContext.span().spanIdString(), mockSpan.id());
        assertEquals(requestContext.span().parentIdString(), mockSpan.parentId());
        checkServerSpan(mockSpan);
    }

    @Test
    public void testAsync() throws InterruptedException {
        ReportSpan mockSpan = runAsyncOne(AsyncContext::complete);
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
        assertNull(mockSpan.parentId());
        checkServerSpan(mockSpan);

        String errorInfo = "timeout eror";
        mockSpan = runAsyncOne(asyncContext -> {
            AsyncEvent asyncEvent = new AsyncEvent(asyncContext, new RuntimeException(errorInfo));
            MockAsyncContext mockAsyncContext = (MockAsyncContext) asyncContext;
            for (AsyncListener asyncListener : mockAsyncContext.getListeners()) {
                try {
                    asyncListener.onTimeout(asyncEvent);
                } catch (IOException e) {
                    throw new RuntimeException("error");
                }
            }
            asyncContext.complete();
        });
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
        assertNull(mockSpan.parentId());
        checkServerSpan(mockSpan);
        assertEquals(errorInfo, mockSpan.tag("error"));
    }

    public ReportSpan runAsyncOne(Consumer<AsyncContext> asyncContextConsumer) throws InterruptedException {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        DoFilterTraceInterceptor doFilterTraceInterceptor = new DoFilterTraceInterceptor();
        doFilterTraceInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        httpServletRequest.setAsyncSupported(true);
        final AsyncContext asyncContext = httpServletRequest.startAsync(httpServletRequest, response);
        doFilterTraceInterceptor.doAfter(methodInfo, EaseAgent.getContext());

        MockEaseAgent.cleanLastSpan();
        Thread thread = new Thread(() -> asyncContextConsumer.accept(asyncContext));
        thread.start();
        thread.join();
        return MockEaseAgent.getLastSpan();
    }

    @Test
    public void doBefore() {
        testTrace();
    }

    @Test
    public void doAfter() {
        testTrace();
    }

    public class MockClientRequest implements Request {
        private final Setter setter;

        public MockClientRequest(Setter setter) {
            this.setter = setter;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
        }

        @Override
        public String header(String name) {
            return null;
        }

        @Override
        public String name() {
            return "test";
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            setter.setHeader(name, value);
        }
    }
}
