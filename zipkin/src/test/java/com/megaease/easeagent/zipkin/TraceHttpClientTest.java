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
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TraceHttpClientTest {

    @Test
    public void should_work() throws Exception {
        final CallTrace trace = new CallTrace();
        final Reporter<Span> reporter = mock(Reporter.class);
        final Tracer tracer = tracer(reporter);
        final String name = "com.megaease.easeagent.zipkin.TraceHttpClientTest$Foo";

        final Definition.Default def = new GenTraceHttpClient().define(Definition.Default.EMPTY);

        trace.push(tracer.newTrace().start());

        final CloseableHttpClient c = (CloseableHttpClient) Classes.transform(name)
                .with(def, trace, new ForwardLock(), tracer)
                .load(getClass().getClassLoader())
                .get(0).newInstance();
        final HttpGet request = new HttpGet("http://localhost");

        c.execute(request);

        Assert.assertNotNull(request.getFirstHeader("X-B3-TraceId"));

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        Assert.assertEquals(span.name(), "http_send");

        final Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("component", "apache-http-client")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "http://localhost")
                .put("remote.address", "127.0.0.1")
                .put("remote.type", "web")
                .put("span.kind", "client")
                .build();
        Assert.assertEquals(span.tags(), map);
        trace.pop();
    }


    private Tracer tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder().spanReporter(reporter).build().tracer();
    }

    static class Foo extends CloseableHttpClient {

        @Override
        protected CloseableHttpResponse doExecute(HttpHost host, HttpRequest req, HttpContext ctx) throws IOException {
            final CloseableHttpResponse res = mock(CloseableHttpResponse.class);
            final StatusLine statusLine = mock(StatusLine.class);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(res.getStatusLine()).thenReturn(statusLine);
            return res;
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public HttpParams getParams() {
            return null;
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return null;
        }
    }
}