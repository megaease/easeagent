package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AbstractAdvice {

    protected final ForwardLock lock;
    protected final AgentInterceptorChain.Builder builder;
    protected final AgentInterceptorChainInvoker chainInvoker;

    public AbstractAdvice(AgentInterceptorChain.Builder builder, AgentInterceptorChainInvoker chainInvoker) {
        this.lock = new ForwardLock();
        this.builder = builder;
        this.chainInvoker = chainInvoker;
    }

    protected ForwardLock.Release<Map<Object, Object>> doEnter(Object invoker, String method, Object[] args) {
        return lock.acquire(() -> {
            Map<Object, Object> context = ContextUtils.createContext();
            if (chainInvoker == null) {
                return context;
            }
            MethodInfo methodInfo = MethodInfo.builder()
                    .invoker(invoker)
                    .method(method)
                    .args(args)
                    .build();
            chainInvoker.doBefore(this.builder, methodInfo, context);
            return context;
        });
    }

    protected Object doExit(ForwardLock.Release<Map<Object, Object>> release, Object invoker, String method, Object[] args, Object retValue, Throwable throwable) {
        AtomicReference<Object> tmpRet = new AtomicReference<>(retValue);
        if (chainInvoker == null) {
            return tmpRet.get();
        }
        release.apply(context -> {
            ContextUtils.setEndTime(context);
            MethodInfo methodInfo = MethodInfo.builder()
                    .invoker(invoker)
                    .method(method)
                    .args(args)
                    .retValue(retValue)
                    .throwable(throwable)
                    .build();
            Object newRetValue = chainInvoker.doAfter(this.builder, methodInfo, context);
            if (newRetValue != retValue) {
                tmpRet.set(newRetValue);
            }
        });
        return tmpRet.get();
    }

    protected void doExitNoRetValue(ForwardLock.Release<Map<Object, Object>> release, Object invoker, String method, Object[] args, Throwable throwable) {
        if (chainInvoker == null) {
            return;
        }
        release.apply(context -> {
            ContextUtils.setEndTime(context);
            MethodInfo methodInfo = MethodInfo.builder()
                    .invoker(invoker)
                    .method(method)
                    .args(args)
                    .throwable(throwable)
                    .build();
            chainInvoker.doAfter(this.builder, methodInfo, context);
        });
    }

    protected void doConstructorExit(Object invoker, String method, Object[] args) {
        Map<Object, Object> context = ContextUtils.createContext();
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(invoker)
                .method(method)
                .args(args)
                .build();
        chainInvoker.doAfter(this.builder, methodInfo, context);
    }
}