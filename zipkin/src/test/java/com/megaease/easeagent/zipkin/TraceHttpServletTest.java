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

import brave.Tracer;
import brave.Tracing;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class TraceHttpServletTest {


    @Test
    @SuppressWarnings("unchecked")
    public void should_work_with_new_context() throws Exception {
        final Reporter<Span> reporter = mock(Reporter.class);
        callService(reporter, request(Collections.<String>emptySet()), response());

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        Assert.assertEquals(span.name(), "http_recv");
//        assertThat(span.annotations().get(0).value(), is("sr"));
//        assertThat(span.annotations().get(1).value(), is("ss"));

        final Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("component", "web")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "/home")
                .put("peer.hostname", "host")
                .put("peer.ipv4", "addr")
                .put("peer.port", "12306")
                .put("remote.address", "addr")
                .put("span.kind", "server")
                .build();
        Map<String, String> tags = span.tags();
        tags.remove("current.milliseconds");
        Assert.assertEquals(tags, map);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_work_with_present_context() throws Exception {
        final ImmutableSet<String> headers = ImmutableSet.of(
                "X-B3-TraceId".toLowerCase(),
                "X-B3-SpanId".toLowerCase()
        );
        final Reporter<Span> reporter = mock(Reporter.class);
        callService(reporter, request(headers), response());

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
//        Assert.assertEquals(span.traceId(), 1L);
        Assert.assertNotNull(span.traceId());
    }

    private void callService(Reporter<Span> reporter, HttpServletRequest request, HttpServletResponse response) throws InstantiationException, IllegalAccessException, ServletException, IOException {
        final CallTrace trace = new CallTrace();
        final Tracer tracer = tracer(reporter);
        final ClassLoader loader = new URLClassLoader(new URL[0]);

        final Definition.Default def = new GenTraceHttpServlet().define(Definition.Default.EMPTY);

        final HttpServlet hs = (HttpServlet) Classes.transform("com.megaease.easeagent.zipkin.TraceHttpServletTest$Foo")
                .with(def, trace, new ForwardLock(), tracer)
                .load(loader).get(0).newInstance();


        hs.service(request, response);
    }

    private HttpServletResponse response() {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        return response;
    }

    private HttpServletRequest request(final Set<String> headers) {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("/home"));

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers));
        when(request.getRemoteHost()).thenReturn("host");
        when(request.getRemoteAddr()).thenReturn("addr");
        when(request.getRemotePort()).thenReturn(12306);
        when(request.getHeader("X-B3-TraceId".toLowerCase())).thenReturn("1");
        when(request.getHeader("X-B3-SpanId".toLowerCase())).thenReturn("2");
        return request;
    }

    private Tracer tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder().spanReporter(reporter).build().tracer();
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        }
    }
}