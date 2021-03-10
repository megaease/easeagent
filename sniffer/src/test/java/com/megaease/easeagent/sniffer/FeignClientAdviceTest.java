package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import feign.Client;
import feign.Request;
import feign.Response;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class FeignClientAdviceTest extends BaseSnifferTest {

    @Test
    public void testInvoke() throws Exception {
        Map<String, Collection<String>> headers = new HashMap<>();
        String url = "http://google.com";
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        Definition.Default def = new GenFeignClientAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyClient client = (MyClient) Classes.transform(baseName + "$MyClient")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4FeignClient", builder))
                .load(loader).get(0).newInstance();

        Request request = Request.create(Request.HttpMethod.GET, url, headers, "ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, null);
        Request.Options options = new Request.Options();

        client.execute(request, options);

        this.verifyInvokeTimes(chainInvoker, 1);

    }

    static class MyClient implements Client {

        @Override
        public Response execute(Request request, Request.Options options) {
            return null;
        }
    }
}
