package com.megaease.easeagent.core.interceptor;

import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Map;

@Slf4j
public class AgentInterceptorChainInvoker {

    private static final String BEFORE_ELAPSED_TIME_KEY = AgentInterceptorChainInvoker.class.getName() + "-BEFORE_ELAPSED_TIME_KEY";
    private static final String BEFORE_BEGIN_TIME_KEY = AgentInterceptorChainInvoker.class.getName() + "-BEFORE_BEGIN_TIME_KEY";
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final AgentInterceptorChainInvoker instance = new AgentInterceptorChainInvoker();

    public static AgentInterceptorChainInvoker getInstance() {
        return instance;
    }

    private boolean logElapsedTime;

    public AgentInterceptorChainInvoker setLogElapsedTime(boolean logElapsedTime) {
        this.logElapsedTime = logElapsedTime;
        return this;
    }

    public void doBefore(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context) {
        long beginTime = System.currentTimeMillis();
        AgentInterceptorChain interceptorChain = this.prepare(builder, context);
        interceptorChain.doBefore(methodInfo, context);
        long elapsed = System.currentTimeMillis() - beginTime;
        context.put(BEFORE_ELAPSED_TIME_KEY, elapsed);
        context.put(BEFORE_BEGIN_TIME_KEY, beginTime);
    }

    public Object doAfter(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context) {
        return doAfter(builder, methodInfo, context, false);
    }

    public Object doAfter(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context, boolean newInterceptorChain) {
        long beginTime4After = System.currentTimeMillis();
        if (newInterceptorChain) {
            context.remove(AgentInterceptorChain.class);
        }
        AgentInterceptorChain interceptorChain = ContextUtils.getFromContext(context, AgentInterceptorChain.class);
        if (interceptorChain == null) {
            interceptorChain = this.prepare(builder, context);
            if (interceptorChain == null) {
                return methodInfo.getRetValue();
            }
            interceptorChain.skipBegin();
        }
        Object result = interceptorChain.doAfter(methodInfo, context);
        this.logTime(methodInfo, context, beginTime4After);
        return result;
    }

    private void logTime(MethodInfo methodInfo, Map<Object, Object> context, long beginTime4After) {
        if (!logElapsedTime) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (methodInfo != null) {
            if (methodInfo.getInvoker() != null) {
                sb.append(methodInfo.getInvoker().getClass().getSimpleName());
            }
            if (methodInfo.getMethod() != null) {
                sb.append("#").append(methodInfo.getMethod());
            }
        }
        Long elapsed4Before = ContextUtils.getFromContext(context, BEFORE_ELAPSED_TIME_KEY);
        Long beginTime4Before = ContextUtils.getFromContext(context, BEFORE_BEGIN_TIME_KEY);
        String beginTime4BeforeStr;
        if (beginTime4Before != null) {
            beginTime4BeforeStr = DateFormatUtils.format(beginTime4Before, DATE_PATTERN);
        } else {
            beginTime4BeforeStr = "";
        }
        String beginTime4AfterStr = DateFormatUtils.format(new Date(beginTime4After), DATE_PATTERN);
        long endTime = System.currentTimeMillis();
        long elapsed4After = endTime - beginTime4After;
        Long beginTime = ContextUtils.getBeginTime(context);
        long elapsedAll = -1;
        if (beginTime != null) {
            elapsedAll = endTime - beginTime;
        }
        log.info("== agent-method:{} before-beginTime:{} elapsed:{}ms, after-beginTime:{} elapsed:{}ms, all-time:{}ms ==",
                sb.toString(), beginTime4BeforeStr, elapsed4Before, beginTime4AfterStr, elapsed4After, elapsedAll);
    }

    private AgentInterceptorChain prepare(AgentInterceptorChain.Builder builder, Map<Object, Object> context) {
        if (builder == null) {
            return null;
        }
        AgentInterceptorChain interceptorChain = builder.build();
        context.put(AgentInterceptorChain.class, interceptorChain);
        return interceptorChain;
    }
}
