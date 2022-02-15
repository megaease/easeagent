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

import com.google.common.collect.ImmutableList;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.internal.WriteBuffer;

import java.util.Collection;

public class AgentV2SpanWriter implements WriteBuffer.Writer<ReportSpan> {

    public final Collection<WriteBuffer.Writer<ReportSpan>> writerList;

    public AgentV2SpanWriter(GlobalExtrasSupplier extrasSupplier) {
        writerList = ImmutableList.<WriteBuffer.Writer<ReportSpan>>builder()
                .add(new AgentV2SpanBaseWriter())
                .add(new AgentV2SpanLocalEndpointWriter())
                .add(new AgentV2SpanRemoteEndpointWriter())
                .add(new AgentV2SpanAnnotationsWriter())
                .add(new AgentV2SpanTagsWriter())
                .add(new AgentV2SpanGlobalWriter("log-tracing", extrasSupplier))
                .build();
    }


    public int sizeInBytes(ReportSpan value) {
        final MutableInt size = new MutableInt(1);
        writerList.forEach(w -> size.add(w.sizeInBytes(value)));
        size.add(1);
        return size.intValue();
    }

    @Override
    public void write(ReportSpan value, WriteBuffer buffer) {
        buffer.writeByte(123);
        writerList.forEach(w -> w.write(value, buffer));
        buffer.writeByte(125);
    }

    public String toString() {
        return "Span";
    }
}
