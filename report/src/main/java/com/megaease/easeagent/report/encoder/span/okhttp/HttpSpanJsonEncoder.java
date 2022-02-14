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
package com.megaease.easeagent.report.encoder.span.okhttp;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import com.megaease.easeagent.report.encoder.span.SpanJsonEncoder;

import java.util.List;

@AutoService(Encoder.class)
public class HttpSpanJsonEncoder implements Encoder<ReportSpan> {
    public static final String ENCODER_NAME = ReportConfigConst.HTTP_SPAN_JSON_ENCODER_NAME;
    SpanJsonEncoder encoder;

    @Override
    public void init(Config config) {
        this.encoder = new SpanJsonEncoder();
        this.encoder.init(config);
    }

    @Override
    public int sizeInBytes(ReportSpan input) {
        return this.encoder.sizeInBytes(input);
    }

    @Override
    public EncodedData encode(ReportSpan input) {
        return new OkHttpJsonRequestBody(this.encoder.encode(input).getData());
    }

    @Override
    public String name() {
        return ENCODER_NAME;
    }

    @Override
    public EncodedData encodeList(List<EncodedData> encodedItems) {
        EncodedData body = this.encoder.encodeList(encodedItems);
        return new OkHttpJsonRequestBody(body.getData());
    }

    @Override
    public int appendSizeInBytes(List<Integer> sizes, int newMsgSize) {
        return this.encoder.appendSizeInBytes(sizes, newMsgSize);
    }

    @Override
    public int packageSizeInBytes(List<Integer> sizes) {
        return this.encoder.packageSizeInBytes(sizes);
    }
}
