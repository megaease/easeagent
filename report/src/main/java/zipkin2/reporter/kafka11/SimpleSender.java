package zipkin2.reporter.kafka11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;

import java.util.List;

public class SimpleSender extends Sender implements SDKSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSender.class);

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encoding().listSizeInBytes(encodedSpans);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        final byte[] bytes = BytesMessageEncoder.JSON.encode(encodedSpans);
        LOGGER.info("{}", new String(bytes));
        return Call.create(null);
    }

    @Override
    public boolean isClose() {
        return false;
    }
}
