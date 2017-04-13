package com.megaease.easeagent.requests;

import brave.sampler.Sampler;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CaptureHttpRequestTest {
    @Test
    public void should_capture_http_request() throws Exception {
        final Reporter reporter = mock(Reporter.class);

        final Definition.Default def = new GenCaptureHttpRequest().define(Definition.Default.EMPTY);
        final HttpServlet s = (HttpServlet) Classes.transform("com.megaease.easeagent.requests.CaptureHttpRequestTest$Foo")
                                                   .with(def, reporter, new CallTrace(), Sampler.ALWAYS_SAMPLE)
                                                   .load(getClass().getClassLoader())
                                                   .get(0).newInstance();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singleton("Content-Type")));
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getProtocol()).thenReturn("1.1");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        s.service(request, response);

        final Map<String, String> headers = Collections.singletonMap("Content-Type", "application/json");
        final Map<String, String> queries = Collections.emptyMap();
        final ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(reporter).report(eq("/"), eq("GET"), eq(200), eq(headers), eq(queries), captor.capture());
        final Context context = captor.getValue();
        assertThat(context.getIoquery(), is(false));
        assertThat(context.getSignature(), is("com.megaease.easeagent.requests.CaptureHttpRequestTest.Foo#doGet"));
    }

    static class Foo extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }
    }
}