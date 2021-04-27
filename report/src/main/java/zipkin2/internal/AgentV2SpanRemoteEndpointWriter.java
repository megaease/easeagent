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

import zipkin2.Span;

public class AgentV2SpanRemoteEndpointWriter extends AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {
    final String remoteEndpointFieldName = ",\"remoteEndpoint\":";

    @Override
    public int sizeInBytes(Span value) {
        if (value.remoteEndpoint() == null) {
            return 0;
        }
        int size = remoteEndpointFieldName.length();
        size += this.endpointSizeInBytes(value.remoteEndpoint(), false);
        return size;
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        if (value.remoteEndpoint() == null) {
            return;
        }
        buffer.writeAscii(remoteEndpointFieldName);
        this.writeEndpoint(value.remoteEndpoint(), buffer, false);
    }
}
