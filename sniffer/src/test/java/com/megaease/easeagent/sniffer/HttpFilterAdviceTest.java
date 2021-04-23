package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

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
        List<Class<?>> classList = Classes.transform(baseName + "$MyFilter", baseName + "$MyServlet")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4Filter", supplier))
                .load(loader);

        Filter filter = (Filter) classList.get(0).newInstance();
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        this.verifyInvokeTimes(chainInvoker, 1);

        reset(chainInvoker);

        MyServlet myServlet = (MyServlet) classList.get(1).newInstance();
        myServlet.service(httpServletRequest, httpServletResponse);
        this.verifyInvokeTimes(chainInvoker, 1);
    }

    public static class MyFilter implements Filter {


        @Override
        public void init(FilterConfig filterConfig) throws ServletException {

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        }

        @Override
        public void destroy() {

        }
    }

    public static class MyServlet extends HttpServlet {
        @Override
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        }
    }
}
