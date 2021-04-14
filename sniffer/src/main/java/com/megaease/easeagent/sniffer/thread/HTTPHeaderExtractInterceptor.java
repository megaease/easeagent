package com.megaease.easeagent.sniffer.thread;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ThreadLocalCurrentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

public class HTTPHeaderExtractInterceptor implements AgentInterceptor {
    private final CrossThreadPropagationConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public HTTPHeaderExtractInterceptor(CrossThreadPropagationConfig crossThreadPropagationConfig) {
        this.config = crossThreadPropagationConfig;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest request = (HttpServletRequest) methodInfo.getArgs()[0];
        final ThreadLocalCurrentContext.Context ctx = ThreadLocalCurrentContext.createContext();
        final String[] canaryHeaders = this.config.getCanaryHeaders();
        for (String header : canaryHeaders) {
            final String value = request.getHeader(header);
            ctx.put(header, value == null ? "" : value);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("extract ctx:{} from http request with headers:{}", ctx, Arrays.toString(canaryHeaders));
        }
        final ThreadLocalCurrentContext.Scope scope = ThreadLocalCurrentContext.DEFAULT.newScope(ctx);
        context.put(ThreadLocalCurrentContext.Scope.class, scope);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        try {
            return chain.doAfter(methodInfo, context);
        } finally {
            try {
                final ThreadLocalCurrentContext.Scope scope = (ThreadLocalCurrentContext.Scope) context.get(ThreadLocalCurrentContext.Scope.class);
                if (scope != null) {
                    scope.close();
                }
            } catch (Exception e) {
                logger.warn("close ThreadLocalCurrentContext.Scope failure", e);
            }
        }
    }
}
