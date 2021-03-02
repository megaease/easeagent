package com.megaease.easeagent.metrics.model;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

public class ErrorPercentModelGauge implements GaugeMetricModel {
    private BigDecimal m1ErrorPercent;
    private BigDecimal m5ErrorPercent;
    private BigDecimal m15ErrorPercent;

    public ErrorPercentModelGauge(BigDecimal m1ErrorPercent, BigDecimal m5ErrorPercent, BigDecimal m15ErrorPercent) {
        this.m1ErrorPercent = m1ErrorPercent;
        this.m5ErrorPercent = m5ErrorPercent;
        this.m15ErrorPercent = m15ErrorPercent;
    }

    @Override
    public Map<String, Object> toHashMap() {
        return ImmutableMap.<String, Object>builder()
                .put("m1errpct", m1ErrorPercent)
                .put("m5errpct", m5ErrorPercent)
                .put("m15errpct", m15ErrorPercent)
                .build();
    }
}
