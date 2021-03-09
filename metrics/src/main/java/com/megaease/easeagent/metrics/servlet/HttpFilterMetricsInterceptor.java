package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.model.ErrorPercentModelGauge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

public class HttpFilterMetricsInterceptor extends AbstractServerMetric  implements AgentInterceptor {

    public HttpFilterMetricsInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context, AgentInterceptorChain chain) {
        chain.doBefore(invoker, method, args, context);
    }

    @Override
    public Object after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) args[1];
        String httpRoute = ServletUtils.getHttpRouteAttribute(httpServletRequest);
        this.collectMetric(httpRoute, httpServletResponse, throwable, context);
        return chain.doAfter(invoker, method, args, retValue, throwable, context);
    }

    public void collectMetric(String key, HttpServletResponse httpServletResponse, Throwable throwable, Map<Object, Object> context) {
        int status = httpServletResponse.getStatus();
        Timer timer = metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT));
        timer.update(Duration.ofMillis(ContextUtils.getDuration(context)));
        final Meter errorMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR));
        final Meter meter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT));
        Counter errorCounter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR));
        Counter counter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT));
        boolean hasException = throwable != null;
        if (status >= 400 || hasException) {
            errorMeter.mark();
            errorCounter.inc();
        }
        counter.inc();
        meter.mark();

        metricRegistry.gauge(metricNameFactory.gaugeName(key, MetricSubType.DEFAULT), () -> () -> {
            BigDecimal m1ErrorPercent = BigDecimal.ZERO;
            BigDecimal m5ErrorPercent = BigDecimal.ZERO;
            BigDecimal m15ErrorPercent = BigDecimal.ZERO;
            BigDecimal error = BigDecimal.valueOf(errorMeter.getOneMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal n = BigDecimal.valueOf(meter.getOneMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m1ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            error = BigDecimal.valueOf(errorMeter.getFiveMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFiveMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m5ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }

            error = BigDecimal.valueOf(errorMeter.getFifteenMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFifteenMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m15ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            return new ErrorPercentModelGauge(m1ErrorPercent, m5ErrorPercent, m15ErrorPercent);
        });
    }
}
