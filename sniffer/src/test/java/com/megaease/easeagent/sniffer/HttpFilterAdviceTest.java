package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.junit.Test;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpFilterAdviceTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        final Definition.Default def = new GenHttpFilterAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        final ClassLoader loader = new URLClassLoader(new URL[0]);
        final CharacterEncodingFilter filter = (CharacterEncodingFilter) Classes.transform(baseName + "$MyCharacterEncodingFilter")
                .with(def, new QualifiedBean("agentInterceptor4HttpFilter", agentInterceptor))
                .load(loader).get(0).newInstance();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

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

    public static class MyCharacterEncodingFilter extends CharacterEncodingFilter {

        public MyCharacterEncodingFilter() {
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        }
    }
}
