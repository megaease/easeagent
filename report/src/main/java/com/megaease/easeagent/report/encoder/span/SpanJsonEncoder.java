package com.megaease.easeagent.report.encoder.span;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.encoder.JsonEncoder;
import com.megaease.easeagent.report.GlobalExtractor;
import zipkin2.Span;
import zipkin2.internal.JsonCodec;

@AutoService(Encoder.class)
@SuppressWarnings("unused")
public class SpanJsonEncoder extends JsonEncoder<Span> {
    public static final String ENCODER_NAME = ReportConfigConst.SPAN_JSON_ENCODER_NAME;
    AgentV2SpanWriter writer;

    @Override
    public void init(Config config) {
        GlobalExtrasSupplier extrasSupplier = GlobalExtractor.getInstance(EaseAgent.getConfigs());
        writer = new AgentV2SpanWriter(extrasSupplier);
    }

    @Override
    public String name() {
        return ENCODER_NAME;
    }

    @Override
    public int sizeInBytes(Span input) {
        return writer.sizeInBytes(input);
    }

    @Override
    public byte[] encode(Span span) {
        return JsonCodec.write(writer, span);
    }
}
