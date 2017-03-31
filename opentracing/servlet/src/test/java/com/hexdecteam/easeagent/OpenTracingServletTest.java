package com.hexdecteam.easeagent;

import brave.Tracer;
import brave.internal.Platform;
import brave.opentracing.BraveTracer;
import brave.propagation.Propagation;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenTracingServletTest {

    @Test
    public void should_report_root_span() throws Exception {
        final Reporter<Span> reporter = new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("http_recv"));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "servlet", endpoint),
                        BinaryAnnotation.create("span.kind", "server", endpoint),
                        BinaryAnnotation.create("http.url", "/home?a=b", endpoint),
                        BinaryAnnotation.create("http.method", "GET", endpoint),
                        BinaryAnnotation.create("http.status_code", "200", endpoint),
                        BinaryAnnotation.create("peer.hostname", "host", endpoint),
                        BinaryAnnotation.create("peer.ipv4", "addr", endpoint),
                        BinaryAnnotation.create("peer.port", "12306", endpoint),
                        BinaryAnnotation.create("has.error", "false", endpoint),
                        BinaryAnnotation.create("remote.address", "host:12306", endpoint)
                ));
            }
        };

        TraceContext.init(BraveTracer.wrap(brave.Tracer.newBuilder().reporter(reporter).build()));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("/home"));
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.<String>emptySet()));
        when(request.getQueryString()).thenReturn("a=b");
        when(request.getRemoteHost()).thenReturn("host");
        when(request.getRemoteAddr()).thenReturn("addr");
        when(request.getRemotePort()).thenReturn(12306);

        when(response.getStatus()).thenReturn(200);


        ((HttpServlet) Classes.transform(Foo.class)
                              .by(new OpenTracingServlet().feature(null))
                              .load().newInstance()).service(request, response);
    }

    @Test
    public void should_report_child_span() throws Exception {
        final long id = Platform.get().randomLong();
        final Reporter<Span> reporter = new Reporter<Span>() {
            @Override
            public void report(Span span) {
                final Endpoint endpoint = Platform.get().localEndpoint();
                assertThat(span.name, is("http_recv"));
                assertThat(span.parentId, is(id));
                assertThat(span.traceId, is(id));
                assertThat(span.binaryAnnotations, hasItems(
                        BinaryAnnotation.create("component", "servlet", endpoint),
                        BinaryAnnotation.create("span.kind", "server", endpoint),
                        BinaryAnnotation.create("http.url", "/home?a=b", endpoint),
                        BinaryAnnotation.create("http.method", "GET", endpoint),
                        BinaryAnnotation.create("http.status_code", "200", endpoint),
                        BinaryAnnotation.create("peer.hostname", "host", endpoint),
                        BinaryAnnotation.create("peer.ipv4", "addr", endpoint),
                        BinaryAnnotation.create("peer.port", "12306", endpoint)
                ));
            }
        };

        TraceContext.init(BraveTracer.wrap(Tracer.newBuilder().reporter(reporter).build()));

        final Map<String, String> headers = generatedHeaders(id);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("/home"));
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
        when(request.getHeader(anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return headers.get(invocationOnMock.getArgumentAt(0, String.class));
            }
        });

        when(request.getQueryString()).thenReturn("a=b");
        when(request.getRemoteHost()).thenReturn("host");
        when(request.getRemoteAddr()).thenReturn("addr");
        when(request.getRemotePort()).thenReturn(12306);

        when(response.getStatus()).thenReturn(200);


        ((HttpServlet) Classes.transform(Foo.class)
                              .by(new OpenTracingServlet().feature(null))
                              .load().newInstance()).service(request, response);
    }

    private Map<String, String> generatedHeaders(long id) {
        final Map<String, String> headers = new HashMap<String, String>();

        Propagation.B3_STRING.injector(new Propagation.Setter<Map<String, String>, String>() {
            @Override
            public void put(Map<String, String> carrier, String key, String value) {
                carrier.put(key, value);
            }

        }).inject(brave.propagation.TraceContext.newBuilder()
                                                .traceId(id)
                                                .traceIdHigh(0L)
                                                .spanId(id)
                                                .build(), headers);
        return headers;
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { }
    }
}