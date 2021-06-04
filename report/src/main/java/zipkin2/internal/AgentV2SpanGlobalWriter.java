/*
 * Copyright (c) 2017, MegaEase
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
 */

package zipkin2.internal;

import com.megaease.easeagent.report.trace.TraceProps;
import com.megaease.easeagent.report.util.TextUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.Span;

import java.util.Optional;

public class AgentV2SpanGlobalWriter implements WriteBuffer.Writer<Span> {

    final String type;
    final GlobalExtrasSupplier extras;//= ApplicationUtils.getBean(Environment.class).getProperty(MetricNameBuilder
    // .SPRING_APPLICATION_NAME, "");
    final TraceProps traceProperties;//= ApplicationUtils.getBean(TraceProperties.class);

    final String typeFieldName = ",\"type\":\"";
    final String serviceFieldName = ",\"service\":\"";
    final String systemFieldName = ",\"system\":\"";

    public AgentV2SpanGlobalWriter(String type, GlobalExtrasSupplier extras, TraceProps tp) {
        this.type = type;
        this.extras = extras;
        this.traceProperties = tp;
    }

    @Override
    public int sizeInBytes(Span value) {
        final MutableInt mutableInt = new MutableInt(0);
        Optional.ofNullable(traceProperties).ifPresent(t -> {
            if (TextUtils.hasText(type)) {
                mutableInt.add(typeFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(type));
            }

            String tmpService = this.extras.service();
            if (TextUtils.hasText(tmpService)) {
                mutableInt.add(serviceFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpService));
            }

            String tmpSystem = this.extras.system();
            if (TextUtils.hasText(tmpSystem)) {
                mutableInt.add(systemFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpSystem));
            }
        });
        return mutableInt.intValue();
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        Optional.ofNullable(traceProperties).ifPresent(t -> {
            if (TextUtils.hasText(type)) {
                buffer.writeAscii(typeFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(type));
                buffer.writeByte(34);
            }
            String tmpService = this.extras.service();
            if (TextUtils.hasText(tmpService)) {
                buffer.writeAscii(serviceFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(tmpService));
                buffer.writeByte(34);
            }
            String tmpSystem = this.extras.system();
            if (TextUtils.hasText(tmpSystem)) {
                buffer.writeAscii(systemFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(tmpSystem));
                buffer.writeByte(34);
            }
        });
    }
}
