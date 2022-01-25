package com.megaease.easeagent.report.encoder.metric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.encoder.JsonEncoder;

import java.util.Map;

@AutoService(Encoder.class)
public class MetricJsonEncoder extends JsonEncoder<Map<String, Object>> {
    public static final String ENCODER_NAME = ReportConfigConst.METRIC_JSON_ENCODER_NAME;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String name() {
        return ENCODER_NAME;
    }

    @Override
    public void init(Config config) {
        // ignored
    }

    @Override
    public int sizeInBytes(Map<String, Object> input) {
        // ignored for default metric output
        return 0;
    }

    @Override
    public byte[] encode(Map<String, Object> input) {
        try {
            String data = this.objectMapper.writeValueAsString(input);
            return data.getBytes();
        } catch (JsonProcessingException e) {
            // ignored
        }
        return new byte[0];
    }
}
