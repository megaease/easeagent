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
package com.megaease.easeagent.report.encoder.log;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.report.ByteWrapper;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.encoder.JsonEncoder;
import zipkin2.internal.JsonCodec;

@AutoService(Encoder.class)
public class AccessLogJsonEncoder extends JsonEncoder<AccessLogInfo> {
    public static final String ENCODER_NAME = ReportConfigConst.ACCESS_LOG_JSON_ENCODER_NAME;

    AccessLogWriter writer;

    @Override
    public String name() {
        return ENCODER_NAME;
    }

    @Override
    public void init(Config config) {
        this.writer = new AccessLogWriter();
    }

    @Override
    public int sizeInBytes(AccessLogInfo input) {
        if (input.getEncodedData() != null) {
            return input.getEncodedData().size();
        }
        return this.writer.sizeInBytes(input);
    }

    @Override
    public EncodedData encode(AccessLogInfo input) {
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
}
