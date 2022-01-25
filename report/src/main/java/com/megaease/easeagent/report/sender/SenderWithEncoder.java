package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.plugin.report.Callback;
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
    Callback<Void> send(List<byte[]> encodedData);
}
