package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RestTemplateAdviceTest extends BaseSnifferTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        Definition.Default def = new GenRestTemplateAdvice().define(Definition.Default.EMPTY);
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyRequest request = (MyRequest) Classes.transform(baseName + "$MyRequest")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4RestTemplate", builder))
                .load(loader).get(0).newInstance();

        HttpHeaders headers = new HttpHeaders();
        request.executeInternal(headers);

        this.verifyInvokeTimes(chainInvoker, 1);

    }

    public static class MyRequest extends AbstractClientHttpRequest {

        @Override
        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        public String getMethodValue() {
            return null;
        }

        @Override
        public URI getURI() {
            return null;
        }
    }
}
