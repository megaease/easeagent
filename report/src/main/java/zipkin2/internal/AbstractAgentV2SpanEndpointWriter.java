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

import zipkin2.Endpoint;
import zipkin2.Span;

public abstract class AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {

    final String serviceNameFieldName = "\"serviceName\":\"";
    final String ipv4FieldName = "\"ipv4\":\"";
    final String ipv6FieldName = "\"ipv6\":\"";
    final String portFieldName = "\"port\":";

    protected int endpointSizeInBytes(Endpoint value, boolean writeEmptyServiceName) {
        int sizeInBytes = 1; // one byte for {
        String serviceName = value.serviceName();
        if (serviceName == null && writeEmptyServiceName) {
            serviceName = "";
        }

        if (serviceName != null) {
            sizeInBytes += serviceNameFieldName.length() + 1;
            sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(serviceName);
        }

        if (value.ipv4() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += ipv4FieldName.length() + 1;
            sizeInBytes += value.ipv4().length();
        }

        if (value.ipv6() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += ipv6FieldName.length() + 1;
            sizeInBytes += value.ipv6().length();
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += portFieldName.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes((long) port);
        }

        sizeInBytes += 1; // one byte for }
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
            b.writeAscii(serviceNameFieldName);
            b.writeUtf8(JsonEscaper.jsonEscape(serviceName));
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv4() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(ipv4FieldName);
            b.writeAscii(value.ipv4());
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv6() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(ipv6FieldName);
            b.writeAscii(value.ipv6());
            b.writeByte('\"');
            wroteField = true;
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(portFieldName);
            b.writeAscii((long) port);
        }

        b.writeByte('}');
    }
}
