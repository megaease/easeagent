package org.springframework.cloud.openfeign.ribbon;

import com.megaease.easeagent.sniffer.ServiceNamePropagationAdvice;
import com.netflix.client.config.IClientConfig;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

public class Adaptor {
    public void doWork(FeignLoadBalancer balancer, IClientConfig config) throws IOException {
        Client client = Mockito.mock(Client.class);
        String randomText = UUID.randomUUID().toString();
        Mockito.when(config.getClientName()).thenReturn(randomText);

        FeignLoadBalancer.RibbonRequest ribbonRequest = new FeignLoadBalancer.RibbonRequest(client, Request.create(Request.HttpMethod.GET, "http://127.0.0.1/test",
                new HashMap<>(), new byte[0], Charset.defaultCharset(), new RequestTemplate()), URI.create("http://127.0.0.1/test"));
        balancer.execute(ribbonRequest, config);
        Request request = ribbonRequest.toRequest();
        Assert.assertTrue(request.headers().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals(randomText, request.headers().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).iterator().next());
    }
}
