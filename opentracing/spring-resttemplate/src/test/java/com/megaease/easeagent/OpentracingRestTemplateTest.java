package com.megaease.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static com.megaease.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpentracingRestTemplateTest {

    @Test
    public void should_report_root_span() throws Exception {
        final Reporter<Span> reporter = new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("http_send"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "spring-rest-template", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("http.url", "http://www.easeteam.com/index", endpoint),
                        BinaryAnnotation.create("http.method", "GET", endpoint),
                        BinaryAnnotation.create("http.status_code", "200", endpoint),
                        BinaryAnnotation.create("has.error", "false", endpoint),
                        BinaryAnnotation.create("remote.address", "www.easeteam.com", endpoint)
                ));
            }
        };

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));

        final Transformation.Feature feature = new OpentracingRestTemplate().feature(null);

        final Class<ClientHttpRequest> loaded = Classes.<ClientHttpRequest>transform(FooClientHttpRequest.class).by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(loaded)));

        loaded.newInstance().execute();
    }

    public static class FooClientHttpRequest extends AbstractClientHttpRequest {

        @Override
        protected OutputStream getBodyInternal(HttpHeaders httpHeaders) throws IOException {
            return null;
        }

        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders httpHeaders) throws IOException {
            assertThat(httpHeaders.keySet(), hasItems("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"));
            final ClientHttpResponse response = mock(ClientHttpResponse.class);
            when(response.getRawStatusCode()).thenReturn(200);
            return response;
        }

        @Override
        public HttpMethod getMethod() {
            return HttpMethod.GET;
        }

        @Override
        public URI getURI() {
            return URI.create("http://www.easeteam.com/index");
        }
    }
}