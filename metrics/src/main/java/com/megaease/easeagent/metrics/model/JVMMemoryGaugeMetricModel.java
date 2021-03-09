package com.megaease.easeagent.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class JVMMemoryGaugeMetricModel implements GaugeMetricModel {
    private Long bytesInit;
    private Long bytesUsed;
    private Long bytesCommitted;
    private Long bytesMax;

    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("bytes-init", bytesInit);
        result.put("bytes-used", bytesUsed);
        result.put("bytes-committed", bytesCommitted);
        result.put("bytes-max", bytesMax);
        return result;
    }
}
