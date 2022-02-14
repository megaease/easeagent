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

import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import zipkin2.Span;
import zipkin2.internal.WriteBuffer;

public class AgentV2SpanRemoteEndpointWriter extends AbstractAgentV2SpanEndpointWriter
    implements WriteBuffer.Writer<ReportSpan> {
    static final String REMOTE_ENDPOINT_FIELD_NAME = ",\"remoteEndpoint\":";

    @Override
    public int sizeInBytes(ReportSpan value) {
        if (value.remoteEndpoint() == null) {
            return 0;
        }
        int size = REMOTE_ENDPOINT_FIELD_NAME.length();
        size += this.endpointSizeInBytes(value.remoteEndpoint(), false);
        return size;
    }

    @Override
    public void write(ReportSpan value, WriteBuffer buffer) {
        if (value.remoteEndpoint() == null) {
            return;
        }
        buffer.writeAscii(REMOTE_ENDPOINT_FIELD_NAME);
        this.writeEndpoint(value.remoteEndpoint(), buffer, false);
    }
}
