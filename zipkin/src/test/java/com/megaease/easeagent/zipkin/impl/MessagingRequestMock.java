/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.trace.MessagingRequest;

public class MessagingRequestMock extends RequestMock implements MessagingRequest {
    private String operation;
    private String channelKind;
    private String channelName;


    public MessagingRequestMock setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public MessagingRequestMock setChannelKind(String channelKind) {
        this.channelKind = channelKind;
        return this;
    }

    public MessagingRequestMock setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    @Override
    public String operation() {
        return operation;
    }

    @Override
    public String channelKind() {
        return channelKind;
    }

    @Override
    public String channelName() {
        return channelName;
    }

    @Override
    public Object unwrap() {
        return this;
    }
}
