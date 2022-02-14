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
package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Encoder;
import com.megaease.easeagent.plugin.report.Sender;

import java.util.List;

public interface SenderWithEncoder extends Sender {
    <T> Encoder<T> getEncoder();

    /**
     * Sends a list of encoded data to a transport such as http or Kafka.
     *
     * @param encodedData list of encoded data, such as encoded spans.
     * @throws IllegalStateException if {@link #close() close} was called.
     */
    Call<Void> send(List<EncodedData> encodedData);
}
