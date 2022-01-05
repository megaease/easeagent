package com.megaease.easeagent.zipkin.impl.message;

import brave.messaging.ConsumerRequest;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;

public class ZipkinConsumerRequest<R extends MessagingRequest>  extends ConsumerRequest {

    private final R request;

    public ZipkinConsumerRequest(R request) {
        this.request = request;
    }

    @Override
    public String operation() {
        return request.operation();
    }

    @Override
    public String channelKind() {
        return request.channelKind();
    }

    @Override
    public String channelName() {
        return request.channelName();
    }

    @Override
    public Object unwrap() {
        return request.unwrap();
    }
}
