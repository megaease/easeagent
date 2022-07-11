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

package zipkin2.reporter.kafka11;

import zipkin2.Call;
import zipkin2.CheckResult;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.io.IOException;
import java.util.List;

public class SDKKafkaSender extends Sender implements SDKSender {
    private final KafkaSender kafkaSender;

    public SDKKafkaSender(KafkaSender kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    public static SDKKafkaSender wrap(KafkaSender sender) {
        return new SDKKafkaSender(sender);
    }

    @Override
    public boolean isClose() {
        return kafkaSender.closeCalled;
    }

    @Override
    public void close() throws IOException {
        kafkaSender.close();
    }

    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        if (kafkaSender.closeCalled) {
            throw new IllegalStateException("closed");
        } else {
            byte[] message = kafkaSender.encoder.encode(encodedSpans);
            return kafkaSender.new KafkaCall(message);
        }
    }

    public Call<Void> sendSpans(byte[] encodedSpans) {
        if (kafkaSender.closeCalled) {
            throw new IllegalStateException("closed");
        } else {
            return kafkaSender.new KafkaCall(encodedSpans);
        }
    }

    @Override
    public Encoding encoding() {
        return kafkaSender.encoding();
    }

    @Override
    public int messageMaxBytes() {
        return kafkaSender.messageMaxBytes();
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return kafkaSender.messageSizeInBytes(encodedSpans);
    }

    @Override
    public CheckResult check() {
        return kafkaSender.check();
    }

    @Override
    public int messageSizeInBytes(int encodedSizeInBytes) {
        return kafkaSender.messageSizeInBytes(encodedSizeInBytes);
    }
}
