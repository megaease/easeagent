package com.megaease.easeagent.zipkin;

import brave.Tracer;
import com.google.common.base.Function;
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
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import zipkin.BinaryAnnotation;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
        c.execute(new HttpGet("http://localhost"));

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        assertThat(span.name, is("http_send"));
        assertThat(span.annotations.get(0).value, is("cs"));
        assertThat(span.annotations.get(1).value, is("cr"));

        final Iterable<Map.Entry<String, String>> entries = ImmutableMap.<String, String>builder()
                .put("component", "apache-http-client")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "http://localhost")
                .put("remote.address", "localhost")
                .put("span.kind", "client")
                .build().entrySet();
        assertThat(asEntries(span.binaryAnnotations), is(entries));
        trace.pop();
    }

    private Iterable<Map.Entry<String, String>> asEntries(List<BinaryAnnotation> bas) {
        return from(bas).transform(new Function<BinaryAnnotation, Map.Entry<String, String>>() {
            @Override
            public Map.Entry apply(BinaryAnnotation input) {
                return new AbstractMap.SimpleEntry(input.key, new String(input.value));
            }
        }).toSet();
    }

    private Tracer tracer(Reporter<Span> reporter) {
        return Tracer.newBuilder().reporter(reporter).build();
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