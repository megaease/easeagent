package com.megaease.easeagent;

import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TraceServletTest {
    @Test
    public void should_match_foo() throws Exception {
        assertTrue(featureOf(".+").type().matches(new TypeDescription.ForLoadedType(Foo.class)));
    }

    @Test
    public void should_not_trace_servlet_if_regex_is_empty() throws Exception {
        Classes.transform(Foo.class).by(featureOf("")).load();
        assertNull(EventBus.queue.poll(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void should_not_trace_servlet_if_no_head_matched() throws Exception {
        final Class<?> aClass = Classes.transform(Foo.class).by(featureOf("X-Trace")).load();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.<String>emptyList()));

        aClass.getMethod("service", ServletRequest.class, ServletResponse.class)
              .invoke(aClass.newInstance(), request, mock(HttpServletResponse.class));

        assertNull(EventBus.queue.poll(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void should_trace_servlet() throws Exception {
        final Class<?> aClass = Classes.transform(Foo.class).by(featureOf("X-Trace")).load();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singleton("X-Trace")));
        when(request.getRequestURI()).thenReturn("/hi");
        when(request.getMethod()).thenReturn("GET");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        aClass.getMethod("service", ServletRequest.class, ServletResponse.class)
              .invoke(aClass.newInstance(), request, response);

        final HTTPTracedRequest hr = (HTTPTracedRequest) EventBus.queue.poll(10, TimeUnit.MILLISECONDS);
        assertNotNull(hr);
        assertFalse(hr.error());
        assertThat(hr.url(), is("/hi"));
        assertThat(hr.method(), is("GET"));
        assertThat(hr.statusCode(), is(200));
        assertThat(hr.status(), is("OK"));
        assertTrue(hr.callStackJson().getChildren().isEmpty());

    }

    private Transformation.Feature featureOf(final String regex) {
        return new TraceServlet().feature(new TraceServlet.Configuration() {
            @Override
            String tracing_header_regex() {
                return regex;
            }
        });
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { }
    }
}