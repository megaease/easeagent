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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TraceRestTemplateTest  extends BaseZipkinTest{

    @Test
    public void should_work() throws Exception {
        final CallTrace trace = new CallTrace();
        final Reporter<Span> reporter = mock(Reporter.class);
        final Tracer tracer = tracer(reporter);
        final ClassLoader loader = getClass().getClassLoader();
        final String name = "com.megaease.easeagent.zipkin.TraceRestTemplateTest$Foo";

        trace.push(tracer.newTrace().start());
        final Definition.Default def = new GenTraceRestTemplate().define(Definition.Default.EMPTY);
        final ClientHttpRequest req = (ClientHttpRequest) Classes.transform(name).with(def, trace, new ForwardLock(), tracer)
                                                                 .load(loader).get(0).newInstance();

        req.execute();

        Assert.assertNotNull(req.getHeaders().getFirst("X-B3-TraceId"));

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        Assert.assertEquals(span.name(), "http_send");

        final Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("component", "spring-rest-template")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "http://localhost")
                .put("remote.address", "127.0.0.1")
                .put("remote.type", "web")
                .put("span.kind", "client")
                .build();
        Assert.assertEquals(span.tags(),map);
        trace.pop();
    }

    static class Foo extends AbstractClientHttpRequest {

        @Override
        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
            final ClientHttpResponse res = mock(ClientHttpResponse.class);
            when(res.getRawStatusCode()).thenReturn(200);
            return res;
        }

        @Override
        public HttpMethod getMethod() {
            return HttpMethod.GET;
        }

        @Override
        public URI getURI() {
            return URI.create("http://localhost");
        }
    }
}