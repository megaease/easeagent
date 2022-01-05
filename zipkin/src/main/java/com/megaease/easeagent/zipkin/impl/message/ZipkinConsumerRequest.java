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
