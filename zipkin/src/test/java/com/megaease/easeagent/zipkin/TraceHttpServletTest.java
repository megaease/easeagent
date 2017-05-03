package com.megaease.easeagent.zipkin;

import brave.Tracer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import zipkin.BinaryAnnotation;
import zipkin.Span;
import zipkin.reporter.Reporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
        assertThat(span.name, is("http_recv"));
        assertThat(span.annotations.get(0).value, is("sr"));
        assertThat(span.annotations.get(1).value, is("ss"));

        final Iterable<Map.Entry<String, String>> entries = ImmutableMap.<String, String>builder()
                .put("component", "servlet")
                .put("has.error", "false")
                .put("http.method", "GET")
                .put("http.status_code", "200")
                .put("http.url", "/home")
                .put("peer.hostname", "host")
                .put("peer.ipv4", "addr")
                .put("peer.port", "12306")
                .put("remote.address", "host:12306")
                .put("span.kind", "server")
                .build().entrySet();
        assertThat(asEntries(span.binaryAnnotations), is(entries));
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
        assertThat(span.traceId, is(1L));
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


    private Iterable<Map.Entry<String, String>> asEntries(List<BinaryAnnotation> bas) {
        return from(bas).transform(new Function<BinaryAnnotation, Map.Entry<String, String>>() {
            @Override
            public Map.Entry<String, String> apply(BinaryAnnotation input) {
                return new AbstractMap.SimpleEntry<String, String>(input.key, new String(input.value));
            }
        }).toSet();
    }

    private Tracer tracer(Reporter<Span> reporter) {
        return Tracer.newBuilder().reporter(reporter).build();
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { }
    }
}