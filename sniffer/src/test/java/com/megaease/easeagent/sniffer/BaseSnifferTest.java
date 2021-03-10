package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseSnifferTest {

    protected void verifyInvokeTimes(AgentInterceptorChainInvoker chainInvoker, int n) {
        verify(chainInvoker, times(n))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(MethodInfo.class),
                        any(Map.class));

        verify(chainInvoker, times(n))
                .doAfter(any(MethodInfo.class),
                        any(Map.class));
    }
}
