package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.mockito.Mockito.*;

public class HttpServletAdviceTest {

    @Test
    public void success() throws Exception {
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        final Definition.Default def = new GenHttpServletAdvice().define(Definition.Default.EMPTY);
        String baseName = HttpServletAdviceTest.class.getName();
        final ClassLoader loader = new URLClassLoader(new URL[0]);
        final HttpServlet httpServlet = (HttpServlet) Classes.transform(baseName + "$MyHttpServlet")
                .with(def, new QualifiedBean("agentInterceptor4HttpServlet", agentInterceptor))
                .load(loader).get(0).newInstance();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        httpServlet.service(httpServletRequest, httpServletResponse);
        verify(agentInterceptor, times(1))
                .before(any(HttpServlet.class), any(String.class),
                        any(Object[].class),
                        any(Map.class));
        verify(agentInterceptor, times(1))
                .after(any(HttpServlet.class), any(String.class),
                        any(Object[].class),
                        any(Object.class), any(Exception.class),
                        any(Map.class));

    }

    public static class MyHttpServlet extends HttpServlet {

        public MyHttpServlet() {
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // do nothing
        }
    }
}
