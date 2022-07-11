/*
 * Copyright (c) 2022, MegaEase
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

import com.megaease.easeagent.zipkin.impl.MessagingRequestMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ZipkinProducerRequestTest {

    @Test
    public void operation() {
        String operation = "operation";
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock().setOperation(operation);
        assertEquals(operation, new ZipkinProducerRequest(messagingRequestMock).operation());
    }

    @Test
    public void channelKind() {
        String channelKind = "channelKind";
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock().setChannelKind(channelKind);
        assertEquals(channelKind, new ZipkinProducerRequest(messagingRequestMock).channelKind());

    }

    @Test
    public void channelName() {
        String channelName = "channelName";
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock().setChannelName(channelName);
        assertEquals(channelName, new ZipkinProducerRequest(messagingRequestMock).channelName());

    }

    @Test
    public void unwrap() {
        MessagingRequestMock messagingRequestMock = new MessagingRequestMock();
        assertEquals(messagingRequestMock, new ZipkinProducerRequest(messagingRequestMock).unwrap());
    }
}
