package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.TextUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
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

    public static final String PROPAGATE_HEAD = "X-MESH-RPC-SERVICE";

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
        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request, @Advice.Argument(1) Object config) {
            try {
                String serviceName = (String) ReflectionTool.invokeMethod(config, "getClientName");
                Object realRequest = ReflectionTool.invokeMethod(request, "getRequest");
                Map<String, Collection<String>> headers = (Map<String, Collection<String>>) ReflectionTool.extractField(realRequest, "headers");
                headers.put(PROPAGATE_HEAD, Collections.singleton(serviceName));
            } catch (Exception e) {
                //ate it
//                e.printStackTrace();
            }
        }
    }

    static class FeignBlockingLoadBalancerClientExecute {
        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request) {
            try {
                String url = (String) ReflectionTool.invokeMethod(request, "url");
                String host = URI.create(url).getHost();
                if (TextUtils.hasText(host)) {
                    Map<String, Collection<String>> headers = (Map<String, Collection<String>>) ReflectionTool.extractField(request, "headers");
                    headers.put(PROPAGATE_HEAD, Collections.singleton(host));
                }
            } catch (Exception e) {
                //ate it
            }
        }
    }

    static class RestTemplateIntercept {
        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request) {
            try {
                URI uri = (URI) ReflectionTool.invokeMethod(request, "getURI");
                String host = uri.getHost();
                if (TextUtils.hasText(host)) {
                    Object fakeHeaders = ReflectionTool.invokeMethod(request, "getHeaders");//org.springframework.http.HttpHeaders
                    MultiValueMap<String, String> headers = (MultiValueMap<String, String>) ReflectionTool.extractField(fakeHeaders, "headers");
                    headers.add(PROPAGATE_HEAD, host);
                }
            } catch (Exception e) {
                //ate it
                e.printStackTrace();
            }
        }
    }

    static class WebClientFilter {
        @SuppressWarnings("unchecked")
        @Advice.OnMethodEnter
        void enter(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Object request) {
            try {
                URI uri = (URI) ReflectionTool.invokeMethod(request, "url");
                String host = uri.getHost();
                if (StringUtils.hasText(host)) {
                    Object fakeHeaders = ReflectionTool.invokeMethod(request, "headers");//org.springframework.http.HttpHeaders
                    MultiValueMap<String, String> headers = (MultiValueMap<String, String>) ReflectionTool.extractField(fakeHeaders, "headers");
                    headers.add(PROPAGATE_HEAD, host);
                }
            } catch (Exception e) {
                //ate it
            }
        }
    }

    static class FilteringWebHandlerHandle {
        @Advice.OnMethodEnter
        void enter(@Advice.This Object invoker,@Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] exchanges) {
            try {
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
                //ate it
//                e.printStackTrace();
            }
        }
    }
}
