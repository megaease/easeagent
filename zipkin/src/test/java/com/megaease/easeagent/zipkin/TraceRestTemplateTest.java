package com.megaease.easeagent.zipkin;

import brave.Tracer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import zipkin.BinaryAnnotation;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TraceRestTemplateTest {

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

        assertThat(req.getHeaders().getFirst("X-B3-TraceId"), is(notNullValue()));

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        assertThat(span.name, is("http_send"));
        assertThat(span.annotations.get(0).value, is("cs"));
        assertThat(span.annotations.get(1).value, is("cr"));

        final Iterable<Map.Entry<String, String>> entries = ImmutableMap.<String, String>builder()
                .put("component", "spring-rest-template")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "http://localhost")
                .put("remote.address", "127.0.0.1")
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