package com.megaease.easeagent.report.encoder.log;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.report.ByteWrapper;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.encoder.JsonEncoder;
import zipkin2.internal.JsonCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.ENCODER_KEY;

@AutoService(Encoder.class)
public class LogDataJsonEncoder extends JsonEncoder<AgentLogData> implements ConfigChangeListener {
    public static final String ENCODER_NAME = ReportConfigConst.LOG_DATA_JSON_ENCODER_NAME;

    Config encoderConfig;
    LogDataWriter writer;

    @Override
    public void init(Config config) {
        config.addChangeListener(this);
        this.encoderConfig = new Configs(getEncoderConfig(config.getConfigs()));
        this.writer = new LogDataWriter(this.encoderConfig);
    }

    @Override
    public int sizeInBytes(AgentLogData input) {
        return this.writer.sizeInBytes(input);
    }

    @Override
    public EncodedData encode(AgentLogData input) {
        try {
            EncodedData d = input.getEncodedData();
            if (d == null) {
                d = new ByteWrapper(JsonCodec.write(writer, input));
                input.setEncodedData(d);
            }
            return d;
        } catch (Exception e) {
            return new ByteWrapper(new byte[0]);
        }
    }

    @Override
    public String name() {
        return ENCODER_NAME;
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        if (list.isEmpty()) {
            return;
        }
        Map<String, String> changes = new HashMap<>();
        list.forEach(change -> changes.put(change.getFullName(), change.getNewValue()));
        Map<String, String> encoderChanges = getEncoderConfig(changes);
        if (encoderChanges.isEmpty()) {
            return;
        }
        Map<String, String> cfg = this.encoderConfig.getConfigs();
        cfg.putAll(encoderChanges);
        this.encoderConfig = new Configs(cfg);
        this.writer = new LogDataWriter(this.encoderConfig);
    }

    private Map<String, String> getEncoderConfig(Map<String, String> cfgMap) {
        Map<String, String> encoderMap = new TreeMap<>();

        cfgMap.forEach((k, v) -> {
            if (k.contains(ENCODER_KEY) && !k.endsWith(ENCODER_KEY)) {
                encoderMap.put(k.substring(k.lastIndexOf('.') + 1), v);
            }
        });

        return encoderMap;
    }
}
