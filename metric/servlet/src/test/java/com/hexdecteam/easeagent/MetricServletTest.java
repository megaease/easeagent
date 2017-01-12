package com.hexdecteam.easeagent;

import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricServletTest {

    @Test
    public void should_mark_meter() throws Exception {
        final Class<?> aClass = Classes.transform(Foo.class).by(feature()).load();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/hi");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        final Method method = aClass.getDeclaredMethod("service", HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(aClass.newInstance(), request, response);

        assertThat(poll(), is(new MetricEvents.Mark("request_throughput").tag("url", "/hi")
                                                                         .tag("request_name", method.toString())));
        assertThat(poll(), is(new MetricEvents.Mark("request_throughput").tag("url", "All")
                                                                         .tag("request_name", "All")));

        assertThat(poll(), is(new MetricEvents.Mark("request_throughput").tag("url", "/hi")
                                                                         .tag("http_code", "200")
                                                                         .tag("request_name", method.toString())));

        assertThat(poll(), is(new MetricEvents.Mark("request_throughput").tag("url", "All")
                                                                         .tag("http_code", "All")
                                                                         .tag("request_name", "All")));

    }

    @Test
    public void should_mark_meter_error() throws Exception {
        final Class<?> aClass = Classes.transform(Foo.class).by(feature()).load();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/hi");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(400);

        final Method method = aClass.getDeclaredMethod("service", HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true);
        method.invoke(aClass.newInstance(), request, response);

        assertNotNull(EventBus.queue.poll(1, TimeUnit.MILLISECONDS));
        assertNotNull(EventBus.queue.poll(1, TimeUnit.MILLISECONDS));
        assertNotNull(EventBus.queue.poll(1, TimeUnit.MILLISECONDS));
        assertNotNull(EventBus.queue.poll(1, TimeUnit.MILLISECONDS));

        assertThat(poll(), is(new MetricEvents.Mark("request_error_throughput").tag("url", "/hi")
                                                                               .tag("request_name", method.toString())));
        assertThat(poll(), is(new MetricEvents.Mark("request_error_throughput").tag("url", "All")
                                                                               .tag("request_name", "All")));

    }

    private MetricEvents.Mark poll() throws InterruptedException {
        return (MetricEvents.Mark) EventBus.queue.poll(1, TimeUnit.MILLISECONDS);
    }

    private Transformation.Feature feature() {
        return new MetricServlet().feature(new MetricServlet.NoConfiguration() {});
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { }
    }

}