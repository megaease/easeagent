package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeasureHttpRequestTest {

    @Test
    public void should_work() throws Exception {
        final MetricRegistry registry = new MetricRegistry();
        final Definition.Default def = new GenMeasureHttpRequest().define(Definition.Default.EMPTY);
        final HttpServlet hs = (HttpServlet) Classes.transform("com.megaease.easeagent.metrics.MeasureHttpRequestTest$Foo")
                                                    .with(def, new CallTrace(), new Metrics(registry))
                                                    .load(getClass().getClassLoader()).get(0).newInstance();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("/"));
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(400);

        hs.service(request, response);

        assertThat(registry.meter("request_throughput:request_name=Foo#doGet,url=/").getCount(), is(1L));
        assertThat(registry.meter("request_throughput:request_name=Foo#doGet,url=/,http_code=400").getCount(), is(1L));
        assertThat(registry.meter("request_throughput:request_name=All,url=All").getCount(), is(1L));
        assertThat(registry.meter("request_throughput:request_name=All,url=All,http_code=400").getCount(), is(1L));
        assertThat(registry.meter("request_error_throughput:request_name=Foo#doGet,url=/").getCount(), is(1L));
        assertThat(registry.meter("request_error_throughput:request_name=All,url=All").getCount(), is(1L));
    }

    public static class Foo extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }
    }
}