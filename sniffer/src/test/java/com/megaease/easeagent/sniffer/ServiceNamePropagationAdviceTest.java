package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.Adaptor;
import org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;

import static org.mockito.Matchers.any;


public class ServiceNamePropagationAdviceTest {
    @BeforeClass
    public static void before() {

    }

    @Test
    public void test_feignLoadBalancer_execute() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        ILoadBalancer loadBalancer = Mockito.mock(ILoadBalancer.class);
        IClientConfig config = Mockito.mock(IClientConfig.class);
        Mockito.when(config.get(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArguments()[1];
            }
        });
        Mockito.when(config.get(CommonClientConfigKey.ConnectTimeout)).thenReturn(10000);
        Mockito.when(config.get(CommonClientConfigKey.ReadTimeout)).thenReturn(10000);
        ServerIntrospector introspector = Mockito.mock(ServerIntrospector.class);
        Definition.Default def = new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY);
        FeignLoadBalancer balancer = (FeignLoadBalancer) Classes.transform(ServiceNamePropagationAdvice.FeignLoadBalancer)
                .with(def)
                .load(loader).get(0).getConstructor(ILoadBalancer.class, IClientConfig.class, ServerIntrospector.class)
                .newInstance(loadBalancer, config, introspector);

        new Adaptor().doWork(balancer, config);
    }

    @Test
    public void test_feignBlockingLoadBalancerClient_execute() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        Client client = Mockito.mock(Client.class);
        BlockingLoadBalancerClient blockingLoadBalancerClient = Mockito.mock(BlockingLoadBalancerClient.class);
        Definition.Default def = new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY);
        FeignBlockingLoadBalancerClient feignBlockingLoadBalancerClient = (FeignBlockingLoadBalancerClient) Classes.transform(ServiceNamePropagationAdvice.FeignBlockingLoadBalancerClient)
                .with(def)
                .load(loader).get(0).getConstructor(Client.class, BlockingLoadBalancerClient.class)
                .newInstance(client, blockingLoadBalancerClient);
        String serviceName = "test-hello";
        Request request = Request.create(Request.HttpMethod.GET, "http://" + serviceName + "/test",
                new HashMap<>(), new byte[0], Charset.defaultCharset(), new RequestTemplate());
        feignBlockingLoadBalancerClient.execute(request, new Request.Options());
        Assert.assertTrue(request.headers().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals(serviceName, request.headers().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).iterator().next());
    }

    @Test
    public void test_retryLoadBalancerInterceptor_intercept() throws Exception {
        ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
        Mockito.when(response.getRawStatusCode()).thenReturn(200);

        LoadBalancerClient loadBalancer = Mockito.mock(LoadBalancerClient.class);
        Mockito.when(loadBalancer.execute(any(), any(), any())).thenReturn(response);

        LoadBalancerRetryProperties lbProperties = Mockito.mock(LoadBalancerRetryProperties.class);
        LoadBalancerRequestFactory requestFactory = Mockito.mock(LoadBalancerRequestFactory.class);
        LoadBalancedRetryFactory lbRetryFactory = Mockito.mock(LoadBalancedRetryFactory.class);
        RetryLoadBalancerInterceptor retryLoadBalancerInterceptor = (RetryLoadBalancerInterceptor) Classes.
                transform(ServiceNamePropagationAdvice.RetryLoadBalancerInterceptor)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class, LoadBalancerRetryProperties.class, LoadBalancerRequestFactory.class, LoadBalancedRetryFactory.class)
                .newInstance(loadBalancer, lbProperties, requestFactory, lbRetryFactory);
        MyRequest request = new MyRequest("http://test-hello/world");
        ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
        retryLoadBalancerInterceptor.intercept(request, new byte[0], execution);

        Assert.assertTrue(request.getHeaders().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.getHeaders().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    @Test
    public void test_loadBalancerInterceptor_intercept() throws Exception {
        ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
        Mockito.when(response.getRawStatusCode()).thenReturn(200);

        LoadBalancerClient loadBalancer = Mockito.mock(LoadBalancerClient.class);
        Mockito.when(loadBalancer.execute(any(), any(), any())).thenReturn(response);


        LoadBalancerRequestFactory requestFactory = Mockito.mock(LoadBalancerRequestFactory.class);
        LoadBalancerInterceptor interceptor = (LoadBalancerInterceptor) Classes.
                transform(ServiceNamePropagationAdvice.LoadBalancerInterceptor)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class, LoadBalancerRequestFactory.class)
                .newInstance(loadBalancer, requestFactory);
        MyRequest request = new MyRequest("http://test-hello/world");
        ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
        interceptor.intercept(request, new byte[0], execution);

        Assert.assertTrue(request.getHeaders().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.getHeaders().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    @Test
    public void test_asyncLoadBalancerInterceptor_intercept() throws Exception {
        ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
        Mockito.when(response.getRawStatusCode()).thenReturn(200);

        LoadBalancerClient loadBalancer = Mockito.mock(LoadBalancerClient.class);
        Mockito.when(loadBalancer.execute(any(), any(), any())).thenReturn(response);

        AsyncLoadBalancerInterceptor interceptor = (AsyncLoadBalancerInterceptor) Classes.
                transform(ServiceNamePropagationAdvice.AsyncLoadBalancerInterceptor)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class)
                .newInstance(loadBalancer);
        MyRequest request = new MyRequest("http://test-hello/world");
        AsyncClientHttpRequestExecution execution = Mockito.mock(AsyncClientHttpRequestExecution.class);
        interceptor.intercept(request, new byte[0], execution);

        Assert.assertTrue(request.getHeaders().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.getHeaders().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    public static class MyRequest extends AbstractClientHttpRequest {
        private URI uri;
        private String method = "GET";

        public MyRequest(String uri) {
            this.uri = URI.create(uri);
        }

        @Override
        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
            return new ByteArrayOutputStream();
        }

        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        public String getMethodValue() {
            return this.method;
        }

        @Override
        public URI getURI() {
            return this.uri;
        }
    }
}