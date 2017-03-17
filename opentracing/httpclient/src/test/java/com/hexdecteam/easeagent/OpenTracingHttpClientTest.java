package com.hexdecteam.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import net.bytebuddy.description.type.TypeDescription;
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
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.io.IOException;

import static com.hexdecteam.easeagent.TraceContext.init;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenTracingHttpClientTest {
    @Test
    public void should_report_root_span() throws Exception {
        final Reporter<Span> reporter = spy(new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("http_send"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "apache-http-client", endpoint),
                        BinaryAnnotation.create("span.kind", "client", endpoint),
                        BinaryAnnotation.create("http.url", "http://www.easeteam.com/index", endpoint),
                        BinaryAnnotation.create("http.method", "GET", endpoint),
                        BinaryAnnotation.create("http.status_code", "200", endpoint)
                ));
            }
        });

        init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));

        final Transformation.Feature feature = new OpenTracingHttpClient().feature(null);

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(FooCloseableHttpClient.class)));

        final Class<CloseableHttpClient> loaded = Classes.<CloseableHttpClient>transform(FooCloseableHttpClient.class).by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(loaded)));

        final HttpGet get = new HttpGet("http://www.easeteam.com/index");
        loaded.newInstance().execute(get);

        verify(reporter).report(any(Span.class));
    }

    public static class FooCloseableHttpClient extends CloseableHttpClient {

        @Override
        protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
                throws IOException {

            assertNotNull(httpRequest.getFirstHeader("X-B3-TraceId"));
            assertNotNull(httpRequest.getFirstHeader("X-B3-SpanId"));
            assertNotNull(httpRequest.getFirstHeader("X-B3-Sampled"));

            final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
            final StatusLine statusLine = mock(StatusLine.class);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(response.getStatusLine()).thenReturn(statusLine);
            return response;
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