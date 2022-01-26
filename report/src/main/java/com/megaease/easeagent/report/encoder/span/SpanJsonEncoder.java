/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
        GlobalExtrasSupplier extrasSupplier = GlobalExtractor.getInstance(EaseAgent.getConfig());
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
