package com.megaease.easeagent.sniffer;

import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseSnifferTest {
    StrictCurrentTraceContext currentTraceContext = StrictCurrentTraceContext.create();

    protected Tracing tracing() {
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build();
        tracing.tracer();
        return tracing;
    }

    protected void verifyInvokeTimes(AgentInterceptorChainInvoker chainInvoker, int n) {
        verify(chainInvoker, times(n))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));

        verify(chainInvoker, times(n))
                .doAfter(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));
    }

    protected void verifyInterceptorTimes(AgentInterceptor agentInterceptor, int n, boolean verifyBefore) {
        if (verifyBefore) {
            verify(agentInterceptor, times(n))
                    .before(any(MethodInfo.class), any(Map.class),
                            any(AgentInterceptorChain.class));
        }
        verify(agentInterceptor, times(n))
                .after(any(MethodInfo.class), any(Map.class),
                        any(AgentInterceptorChain.class));
    }
}
