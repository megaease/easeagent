package com.megaease.easeagent.zipkin;

import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.reactive.AgentGlobalFilter;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();

        AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();
        AgentInterceptorChain.Builder headersChainBuilder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));

        SpringGatewayInitGlobalFilterInterceptor interceptor = new SpringGatewayInitGlobalFilterInterceptor(headersChainBuilder, chainInvoker);
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(interceptor);

        Map<Object, Object> context = ContextUtils.createContext();
        List<GlobalFilter> list = new ArrayList<>();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(null)
                .method("filteringWebHandler")
                .args(new Object[]{list})
                .retValue(null)
                .throwable(null)
                .build();

        chainInvoker.doBefore(builder, methodInfo, context);

        Assert.assertTrue(interceptor.isLoadAgentFilter());
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0) instanceof AgentGlobalFilter);

    }

    @Test
    public void fail() {
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();

        AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();
        AgentInterceptorChain.Builder headersChainBuilder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));

        SpringGatewayInitGlobalFilterInterceptor interceptor = new SpringGatewayInitGlobalFilterInterceptor(headersChainBuilder, chainInvoker);
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(interceptor);

        Map<Object, Object> context = ContextUtils.createContext();
        List<GlobalFilter> list = new ArrayList<>();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(null)
                .method("filteringWebHandler00")
                .args(new Object[]{list})
                .retValue(null)
                .throwable(null)
                .build();

        chainInvoker.doBefore(builder, methodInfo, context);

        Assert.assertFalse(interceptor.isLoadAgentFilter());
        Assert.assertTrue(list.isEmpty());

    }
}
