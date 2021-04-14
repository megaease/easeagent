package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.TextUtils;
import com.megaease.easeagent.core.utils.ThreadLocalCurrentContext;
import com.megaease.easeagent.sniffer.thread.CrossThreadPropagationConfig;
import feign.Request;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class ServiceNamePropagationAdvice implements Transformation {
    public static final String FeignLoadBalancer = "org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer";
    public static final String FeignBlockingLoadBalancerClient = "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient";
    public static final String RetryLoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor";
    public static final String AsyncLoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.AsyncLoadBalancerInterceptor";
    public static final String LoadBalancerInterceptor = "org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor";
    public static final String ReactorLoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction";
    public static final String LoadBalancerExchangeFilterFunction = "org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction";
    public static final String FilteringWebHandler = "org.springframework.cloud.gateway.handler.FilteringWebHandler";

    public static final String PROPAGATE_HEAD = "X-Mesh-RPC-Service";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(named(FeignLoadBalancer))
                // OpenFeign
                .transform(feignLoadBalancerExecute(named("execute").and(takesArguments(2))
                        .and(takesArgument(0, named(FeignLoadBalancer + "$RibbonRequest")))
                        .and(takesArgument(1, named("com.netflix.client.config.IClientConfig")))
                ))
                .type(named(FeignBlockingLoadBalancerClient))
                .transform(feignBlockingLoadBalancerClientExecute(named("execute").and(takesArguments(2))
                        .and(takesArgument(0, named("feign.Request")))
                ))
                // RestTemplate
                .type(namedOneOf(RetryLoadBalancerInterceptor, AsyncLoadBalancerInterceptor, LoadBalancerInterceptor))
                .transform(restTemplateIntercept(named("intercept").and(takesArguments(3))
                        .and(takesArgument(0, named("org.springframework.http.HttpRequest")))
                ))
                // WebClient
                .type(namedOneOf(ReactorLoadBalancerExchangeFilterFunction, LoadBalancerExchangeFilterFunction))
                .transform(webClientFilter(named("filter").and(takesArguments(2))
                        .and(takesArgument(0, named("org.springframework.web.reactive.function.client.ClientRequest")))
                ))
                // Spring Cloud Gateway
                .type(named(FilteringWebHandler))
                .transform(filteringWebHandlerHandle(named("handle").and(takesArguments(1))
                        .and(takesArgument(0, named("org.springframework.web.server.ServerWebExchange")))
                ))
                .end();
    }

    @AdviceTo(ServiceNamePropagationAdvice.FeignLoadBalancerExecute.class)
    abstract Definition.Transformer feignLoadBalancerExecute(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ServiceNamePropagationAdvice.FeignBlockingLoadBalancerClientExecute.class)
    abstract Definition.Transformer feignBlockingLoadBalancerClientExecute(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ServiceNamePropagationAdvice.RestTemplateIntercept.class)
    abstract Definition.Transformer restTemplateIntercept(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ServiceNamePropagationAdvice.WebClientFilter.class)
    abstract Definition.Transformer webClientFilter(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(ServiceNamePropagationAdvice.FilteringWebHandlerHandle.class)
    abstract Definition.Transformer filteringWebHandlerHandle(ElementMatcher<? super MethodDescription> matcher);


    static class FeignLoadBalancerExecute {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final CrossThreadPropagationConfig config;

        @Injection.Autowire
        public FeignLoadBalancerExecute(CrossThreadPropagationConfig config) {
            this.config = config;
        }

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request, @Advice.Argument(1) Object config) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("enter method [{}]", method);
                }
                String serviceName = (String) ReflectionTool.invokeMethod(config, "getClientName");
                Object realRequest = ReflectionTool.invokeMethod(request, "getRequest");
                Map<String, Collection<String>> headers = (Map<String, Collection<String>>) ReflectionTool.extractField(realRequest, "headers");
                headers.put(PROPAGATE_HEAD, Collections.singleton(serviceName));
                ThreadLocalCurrentContext.DEFAULT.fill((k, v) -> headers.put(k, Collections.singleton(v)), this.config.getCanaryHeaders());
            } catch (Exception e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

    static class FeignBlockingLoadBalancerClientExecute {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final CrossThreadPropagationConfig config;

        @Injection.Autowire
        public FeignBlockingLoadBalancerClientExecute(CrossThreadPropagationConfig config) {
            this.config = config;
        }

        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("enter method [{}]", method);
                }
                feign.Request request = (Request) args[0];
                String url = request.url();
                String host = URI.create(url).getHost();
                if (TextUtils.hasText(host)) {
                    final HashMap<String, Collection<String>> newHeaders = new HashMap<>(request.headers());
                    newHeaders.put(PROPAGATE_HEAD, Collections.singleton(host));
                    ThreadLocalCurrentContext.DEFAULT.fill((k, v) -> newHeaders.put(k, Collections.singleton(v)), this.config.getCanaryHeaders());
                    final Request newRequest = Request.create(request.httpMethod(), request.url(), newHeaders, request.body(), request.charset(), request.requestTemplate());
                    args[0] = newRequest;
                }
            } catch (Exception e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

    static class RestTemplateIntercept {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final CrossThreadPropagationConfig config;

        @Injection.Autowire
        public RestTemplateIntercept(CrossThreadPropagationConfig config) {
            this.config = config;
        }

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("enter method [{}]", method);
                }
                URI uri = (URI) ReflectionTool.invokeMethod(request, "getURI");
                String host = uri.getHost();
                if (TextUtils.hasText(host)) {
                    Object fakeHeaders = ReflectionTool.invokeMethod(request, "getHeaders");//org.springframework.http.HttpHeaders
                    MultiValueMap<String, String> headers = (MultiValueMap<String, String>) ReflectionTool.extractField(fakeHeaders, "headers");
                    headers.add(PROPAGATE_HEAD, host);
                    ThreadLocalCurrentContext.DEFAULT.fill(headers::add, this.config.getCanaryHeaders());
                }
            } catch (Exception e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

    static class WebClientFilter {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final CrossThreadPropagationConfig config;

        @Injection.Autowire
        public WebClientFilter(CrossThreadPropagationConfig config) {
            this.config = config;
        }

        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("enter method [{}]", method);
                }
                URI uri = (URI) ReflectionTool.invokeMethod(request, "url");
                String host = uri.getHost();
                if (StringUtils.hasText(host)) {
                    Object fakeHeaders = ReflectionTool.invokeMethod(request, "headers");//org.springframework.http.HttpHeaders
                    MultiValueMap<String, String> headers = (MultiValueMap<String, String>) ReflectionTool.extractField(fakeHeaders, "headers");
                    headers.add(PROPAGATE_HEAD, host);
                    ThreadLocalCurrentContext.DEFAULT.fill(headers::add, this.config.getCanaryHeaders());
                }
            } catch (Exception e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }

    static class FilteringWebHandlerHandle {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Advice.OnMethodEnter
        void enter(@Advice.Origin String method, @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] exchanges) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("enter method [{}]", method);
                }
                ServerWebExchange exchange = (ServerWebExchange) exchanges[0];
                org.springframework.cloud.gateway.route.Route route = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute");
                if (route == null) {
                    return;
                }
                URI uri = route.getUri();
                String scheme = uri.getScheme();
                if (!scheme.equals("lb")) {
                    return;
                }
                String host = uri.getHost();
                if (!StringUtils.hasText(host)) {
                    return;
                }
                ServerHttpRequest newRequest = exchange.getRequest().mutate().header(PROPAGATE_HEAD, host).build();
                ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                exchanges[0] = newExchange;
            } catch (Exception e) {
                logger.warn("intercept method [{}] failure", method, e);
            }
        }
    }
}
