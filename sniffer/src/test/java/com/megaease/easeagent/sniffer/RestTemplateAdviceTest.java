package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RestTemplateAdviceTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        Definition.Default def = new GenRestTemplateAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyRequest request = (MyRequest) Classes.transform(baseName + "$MyRequest")
                .with(def, new QualifiedBean("agentInterceptor4RestTemplate", agentInterceptor))
                .load(loader).get(0).newInstance();

        HttpHeaders headers = new HttpHeaders();
        request.executeInternal(headers);

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
