package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpFilterAdviceTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        Definition.Default def = new GenHttpFilterAdvice().define(Definition.Default.EMPTY);
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        CharacterEncodingFilter filter = (CharacterEncodingFilter) Classes.transform(baseName + "$MyCharacterEncodingFilter")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4Filter", builder))
                .load(loader).get(0).newInstance();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(chainInvoker, times(1))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(HttpServlet.class), any(String.class),
                        any(Object[].class),
                        any(Map.class));
        verify(chainInvoker, times(1))
                .doAfter(any(HttpServlet.class), any(String.class),
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
