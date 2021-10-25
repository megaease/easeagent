package com.megaease.easeagent.sniffer.impl.tracing;

import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.trace.Message;

import javax.annotation.Nonnull;

public class MessageImpl implements Message<TraceContextOrSamplingFlags> {
    private final TraceContextOrSamplingFlags msg;

    public MessageImpl(@Nonnull TraceContextOrSamplingFlags msg) {
        this.msg = msg;
    }

    @Override
    public TraceContextOrSamplingFlags get() {
        return msg;
    }
}
