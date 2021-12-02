package easeagent.plugin.spring.gateway.interceptor.metric;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import easeagent.plugin.spring.gateway.advice.AgentGlobalFilterAdvice;

// @AdviceTo(AgentGlobalFilterAdvice.class)
public class GatewayAccessLogInterceptor implements Interceptor {
    private static Reporter reportConsumer;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        reportConsumer = EaseAgent.metricReporter(config);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {

    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
    }

    @Override
    public String getName() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
