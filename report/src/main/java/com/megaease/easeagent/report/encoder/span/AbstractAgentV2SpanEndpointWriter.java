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

import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

public abstract class AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {

    static final String SERVICE_NAME_FIELD_NAME = "\"serviceName\":\"";
    static final String IPV4_FIELD_NAME = "\"ipv4\":\"";
    static final String IPV6_FIELD_NAME = "\"ipv6\":\"";
    static final String PORT_FIELD_NAME = "\"port\":";

    protected int endpointSizeInBytes(Endpoint value, boolean writeEmptyServiceName) {
        int sizeInBytes = 1;
        String serviceName = value.serviceName();
        if (serviceName == null && writeEmptyServiceName) {
            serviceName = "";
        }

        if (serviceName != null) {
            sizeInBytes += SERVICE_NAME_FIELD_NAME.length() + 1;
            sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(serviceName);
        }

        if (value.ipv4() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += IPV4_FIELD_NAME.length() + 1;
            sizeInBytes += value.ipv4().length();
        }

        if (value.ipv6() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += IPV6_FIELD_NAME.length() + 1;
            sizeInBytes += value.ipv6().length();
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += PORT_FIELD_NAME.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes(port);
        }

        sizeInBytes += 1;
        return sizeInBytes;
    }

    protected void writeEndpoint(Endpoint value, WriteBuffer b, boolean writeEmptyServiceName) {

        b.writeByte('{');
        boolean wroteField = false;
        String serviceName = value.serviceName();
        if (serviceName == null && writeEmptyServiceName) {
            serviceName = "";
        }

        if (serviceName != null) {
            b.writeAscii(SERVICE_NAME_FIELD_NAME);
            b.writeUtf8(JsonEscaper.jsonEscape(serviceName));
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv4() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(IPV4_FIELD_NAME);
            b.writeAscii(value.ipv4());
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv6() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(IPV6_FIELD_NAME);
            b.writeAscii(value.ipv6());
            b.writeByte('\"');
            wroteField = true;
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(PORT_FIELD_NAME);
            b.writeAscii(port);
        }

        b.writeByte('}');
    }
}
