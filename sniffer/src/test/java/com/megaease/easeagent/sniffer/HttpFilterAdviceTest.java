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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@SuppressWarnings("all")
public class HttpFilterAdviceTest extends BaseSnifferTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        Definition.Default def = new GenHttpFilterAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
        ClassLoader loader = this.getClass().getClassLoader();
        CharacterEncodingFilter filter = (CharacterEncodingFilter) Classes.transform(baseName + "$MyCharacterEncodingFilter")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4Filter", supplier))
                .load(loader).get(0).newInstance();

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        this.verifyInvokeTimes(chainInvoker, 1);

    }

    public static class MyCharacterEncodingFilter extends CharacterEncodingFilter {

        public MyCharacterEncodingFilter() {
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        }
    }
}
