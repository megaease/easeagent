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

package com.megaease.easeagent.report.encoder.span;

import com.megaease.easeagent.report.util.TextUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

public class AgentV2SpanGlobalWriter implements WriteBuffer.Writer<Span> {

    final String type;
    final GlobalExtrasSupplier extras;

    static final String TYPE_FIELD_NAME = ",\"type\":\"";
    static final String SERVICE_FIELD_NAME = ",\"service\":\"";
    static final String SYSTEM_FIELD_NAME = ",\"system\":\"";

    public AgentV2SpanGlobalWriter(String type, GlobalExtrasSupplier extras) {
        this.type = type;
        this.extras = extras;
    }

    @Override
    public int sizeInBytes(Span value) {
        final MutableInt mutableInt = new MutableInt(0);
        if (TextUtils.hasText(type)) {
            mutableInt.add(TYPE_FIELD_NAME.length() + 1);
            mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(type));
        }

        String tmpService = this.extras.service();
        if (TextUtils.hasText(tmpService)) {
            mutableInt.add(SERVICE_FIELD_NAME.length() + 1);
            mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpService));
        }

        String tmpSystem = this.extras.system();
        if (TextUtils.hasText(tmpSystem)) {
            mutableInt.add(SYSTEM_FIELD_NAME.length() + 1);
            mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpSystem));
        }
        return mutableInt.intValue();
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        if (TextUtils.hasText(type)) {
            buffer.writeAscii(TYPE_FIELD_NAME);
            buffer.writeUtf8(JsonEscaper.jsonEscape(type));
            buffer.writeByte(34);
        }
        String tmpService = this.extras.service();
        if (TextUtils.hasText(tmpService)) {
            buffer.writeAscii(SERVICE_FIELD_NAME);
            buffer.writeUtf8(JsonEscaper.jsonEscape(tmpService));
            buffer.writeByte(34);
        }
        String tmpSystem = this.extras.system();
        if (TextUtils.hasText(tmpSystem)) {
            buffer.writeAscii(SYSTEM_FIELD_NAME);
            buffer.writeUtf8(JsonEscaper.jsonEscape(tmpSystem));
            buffer.writeByte(34);
        }
    }
}
