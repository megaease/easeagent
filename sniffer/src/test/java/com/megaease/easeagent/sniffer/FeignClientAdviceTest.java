package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import feign.Client;
import feign.Request;
import feign.Response;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FeignClientAdviceTest {

    @Test
    public void testInvoke() throws Exception {
        Map<String, Collection<String>> headers = new HashMap<>();
        String url = "http://google.com";
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        Definition.Default def = new GenFeignClientAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyClient client = (MyClient) Classes.transform(baseName + "$MyClient")
                .with(def, new QualifiedBean("agentInterceptor4FeignClient", agentInterceptor))
                .load(loader).get(0).newInstance();

        Request request = Request.create(Request.HttpMethod.GET, url, headers, "ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, null);
        Request.Options options = new Request.Options();

        client.execute(request, options);

        verify(agentInterceptor, times(1))
                .before(any(), any(String.class),
                        any(Object[].class),
                        any(Map.class));
        verify(agentInterceptor, times(1))
                .after(any(), any(String.class),
                        any(Object[].class),
                        any(Object.class), any(Exception.class),
                        any(Map.class));

    }

    static class MyClient implements Client {

        @Override
        public Response execute(Request request, Request.Options options) {
            return null;
        }
    }
}
