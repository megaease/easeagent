package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractAdvice {

    protected final ForwardLock lock;
    protected final AgentInterceptorChain.Builder builder;
    protected final AgentInterceptorChainInvoker agentInterceptorChainInvoker;

    public AbstractAdvice(AgentInterceptorChain.Builder builder, AgentInterceptorChainInvoker agentInterceptorChainInvoker) {
        this.lock = new ForwardLock();
        this.builder = builder;
        this.agentInterceptorChainInvoker = agentInterceptorChainInvoker;
    }

    @Advice.OnMethodEnter
    protected ForwardLock.Release<Map<Object, Object>> innerEnter(
            @Advice.This Object invoker,
            @Advice.Origin("#m") String method,
            @Advice.AllArguments Object[] args
    ) {
        return lock.acquire(() -> {
            Map<Object, Object> context = ContextUtils.createContext();
            if (agentInterceptorChainInvoker == null) {
                return context;
            }
            MethodInfo methodInfo = MethodInfo.builder()
                    .invoker(invoker)
                    .method(method)
                    .args(args)
                    .build();
            agentInterceptorChainInvoker.doBefore(this.builder, methodInfo, context);
            return context;
        });
    }

    protected Object innerExit(
            @Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
            @Advice.This Object invoker,
            @Advice.Origin("#m") String method,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
            @Advice.Thrown Throwable throwable
    ) {
        AtomicReference<Object> tmpRet = new AtomicReference<>(retValue);
        if (agentInterceptorChainInvoker == null) {
            return tmpRet.get();
        }
        release.apply(context -> {
            MethodInfo methodInfo = ContextUtils.getFromContext(context, MethodInfo.class);
            methodInfo.setRetValue(retValue);
            methodInfo.setThrowable(throwable);
            Object newRetValue = agentInterceptorChainInvoker.doAfter(methodInfo, context);
            if (newRetValue != retValue) {
                tmpRet.set(newRetValue);
            }
        });
        return tmpRet.get();
    }

    protected void innerExitNoRetValue(
            @Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
            @Advice.This Object invoker,
            @Advice.Origin("#m") String method,
            @Advice.AllArguments Object[] args,
            @Advice.Thrown Throwable throwable
    ) {
        release.apply(context -> {
            MethodInfo methodInfo = ContextUtils.getFromContext(context, MethodInfo.class);
            methodInfo.setThrowable(throwable);
            if (agentInterceptorChainInvoker == null) {
                return;
            }
            agentInterceptorChainInvoker.doAfter(methodInfo, context);
        });
    }

}
