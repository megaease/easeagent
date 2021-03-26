package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.sniffer.thread.CrossThreadPropagationConfig;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.Adaptor;
import org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;


public class ServiceNamePropagationAdviceTest {

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
                .with(def, new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(loader).get(0).getConstructor(ILoadBalancer.class, IClientConfig.class, ServerIntrospector.class)
                .newInstance(loadBalancer, config, introspector);

        new Adaptor().doWork(balancer, config);
    }

    private CrossThreadPropagationConfig getCrossThreadPropagationConfig() {
        final HashMap<String, String> source = new HashMap<>();
        source.put(ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS + ".x-canary-labels.hello", "test");
        final CrossThreadPropagationConfig bean = new CrossThreadPropagationConfig(new Configs(source));
        return bean;
    }

    @Test
    public void test_feignBlockingLoadBalancerClient_execute() throws Exception {
        String serviceName = "test-hello";
        ClassLoader loader = getClass().getClassLoader();
        Client client = Mockito.mock(Client.class);
        BlockingLoadBalancerClient blockingLoadBalancerClient = Mockito.mock(BlockingLoadBalancerClient.class);
        final DefaultServiceInstance defaultServiceInstance = new DefaultServiceInstance("test-instance", serviceName, "localhost", 22370, false);
        Mockito.when(blockingLoadBalancerClient.choose(any())).thenReturn(defaultServiceInstance);
        Mockito.when(blockingLoadBalancerClient.reconstructURI(any(), any())).thenReturn(URI.create("http://localhost:22370/test"));
        Definition.Default def = new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY);
        FeignBlockingLoadBalancerClient feignBlockingLoadBalancerClient = (FeignBlockingLoadBalancerClient) Classes.transform(ServiceNamePropagationAdvice.FeignBlockingLoadBalancerClient)
                .with(def, new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(loader).get(0).getConstructor(Client.class, BlockingLoadBalancerClient.class)
                .newInstance(client, blockingLoadBalancerClient);

        feignBlockingLoadBalancerClient.execute(Request.create(Request.HttpMethod.GET, "http://" + serviceName + "/test",
                new HashMap<>(), new byte[0], Charset.defaultCharset(), new RequestTemplate()), new Request.Options());
        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);

        Mockito.verify(client).execute(captor.capture(), any());
        Request request = captor.getValue();
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
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class, LoadBalancerRetryProperties.class, LoadBalancerRequestFactory.class, LoadBalancedRetryFactory.class)
                .newInstance(loadBalancer, lbProperties, requestFactory, lbRetryFactory);
        MyClientRequest request = new MyClientRequest("http://test-hello/world");
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
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class, LoadBalancerRequestFactory.class)
                .newInstance(loadBalancer, requestFactory);
        MyClientRequest request = new MyClientRequest("http://test-hello/world");
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
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class)
                .newInstance(loadBalancer);
        MyClientRequest request = new MyClientRequest("http://test-hello/world");
        AsyncClientHttpRequestExecution execution = Mockito.mock(AsyncClientHttpRequestExecution.class);
        interceptor.intercept(request, new byte[0], execution);

        Assert.assertTrue(request.getHeaders().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.getHeaders().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    @Test
    public void test_loadBalancerExchangeFilterFunction_filter() throws Exception {
        ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
        Mockito.when(response.getRawStatusCode()).thenReturn(200);

        LoadBalancerClient loadBalancer = Mockito.mock(LoadBalancerClient.class);
        Mockito.when(loadBalancer.execute(any(), any(), any())).thenReturn(response);

        LoadBalancerExchangeFilterFunction function = (LoadBalancerExchangeFilterFunction) Classes.
                transform(ServiceNamePropagationAdvice.LoadBalancerExchangeFilterFunction)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(LoadBalancerClient.class)
                .newInstance(loadBalancer);
        ExchangeFunction exchangeFunction = Mockito.mock(ExchangeFunction.class);

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://test-hello/world")).build();
        function.filter(request, exchangeFunction);

        Assert.assertTrue(request.headers().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.headers().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    @Test
    public void test_reactorLoadBalancerExchangeFilterFunction_filter() throws Exception {
        LoadBalancerClientFactory loadBalancerClientFactory = Mockito.mock(LoadBalancerClientFactory.class);

        ReactorLoadBalancerExchangeFilterFunction function = (ReactorLoadBalancerExchangeFilterFunction) Classes.
                transform(ServiceNamePropagationAdvice.ReactorLoadBalancerExchangeFilterFunction)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(ReactiveLoadBalancer.Factory.class)
                .newInstance(loadBalancerClientFactory);
        ExchangeFunction exchangeFunction = Mockito.mock(ExchangeFunction.class);

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://test-hello/world")).build();
        function.filter(request, exchangeFunction);

        Assert.assertTrue(request.headers().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("test-hello", request.headers().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    @Test
    public void test_filteringWebHandler_filter() throws Exception {
        GlobalFilter filter = Mockito.mock(GlobalFilter.class);
        Mockito.when(filter.filter(any(), any())).thenReturn(Mono.empty());

        FilteringWebHandler handler = (FilteringWebHandler) Classes.
                transform(ServiceNamePropagationAdvice.FilteringWebHandler)
                .with(new GenServiceNamePropagationAdvice().define(Definition.Default.EMPTY), new QualifiedBean("", getCrossThreadPropagationConfig()))
                .load(getClass().getClassLoader()).get(0)
                .getConstructor(List.class)
                .newInstance(Collections.singletonList(filter));

        ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
        WebSessionManager sessionManager = Mockito.mock(WebSessionManager.class);
        WebSession webSession = Mockito.mock(WebSession.class);
        Mockito.when(sessionManager.getSession(any())).thenReturn(Mono.just(webSession));
        ServerCodecConfigurer codecConfigurer = Mockito.mock(ServerCodecConfigurer.class);
        LocaleContextResolver localeContextResolver = Mockito.mock(LocaleContextResolver.class);

        ServerHttpRequest request = new MyServerRequest(URI.create("http://test.com/hello"), "/hello", new HttpHeaders());
        DefaultServerWebExchange exchange = new DefaultServerWebExchange(request, response, sessionManager, codecConfigurer, localeContextResolver);
        Route route = Mockito.mock(Route.class);
        Mockito.when(route.getUri()).thenReturn(URI.create("lb://hello-world"));
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
        handler.handle(exchange).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        Mockito.verify(filter).filter(captor.capture(), any());

        ServerWebExchange captorValue = captor.getValue();
        Assert.assertNotEquals(captorValue, exchange);
        request = captorValue.getRequest();
        Assert.assertTrue(request.getHeaders().containsKey(ServiceNamePropagationAdvice.PROPAGATE_HEAD));
        Assert.assertEquals("hello-world", request.getHeaders().get(ServiceNamePropagationAdvice.PROPAGATE_HEAD).get(0));
    }

    public static class MyClientRequest extends AbstractClientHttpRequest {
        private URI uri;
        private String method = "GET";

        public MyClientRequest(String uri) {
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

    public static class MyServerRequest extends AbstractServerHttpRequest {

        /**
         * Constructor with the URI and headers for the request.
         *
         * @param uri         the URI for the request
         * @param contextPath the context path for the request
         * @param headers     the headers for the request
         */
        public MyServerRequest(URI uri, String contextPath, HttpHeaders headers) {
            super(uri, contextPath, headers);
        }

        @Override
        protected MultiValueMap<String, HttpCookie> initCookies() {
            return new LinkedMultiValueMap<>();
        }

        @Override
        protected SslInfo initSslInfo() {
            return null;
        }

        @Override
        public <T> T getNativeRequest() {
            return null;
        }

        @Override
        public String getMethodValue() {
            return "GET";
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return Flux.empty();
        }
    }
}