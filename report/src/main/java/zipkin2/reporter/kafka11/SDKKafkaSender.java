package zipkin2.reporter.kafka11;

import com.megaease.easeagent.report.trace.TraceProps;
import zipkin2.Call;
import zipkin2.CheckResult;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.io.IOException;
import java.util.List;

public class SDKKafkaSender extends Sender implements SDKSender {
    private KafkaSender kafkaSender;

    private TraceProps traceProperties;

    public SDKKafkaSender(KafkaSender kafkaSender, TraceProps traceProperties) {
        this.kafkaSender = kafkaSender;
        this.traceProperties = traceProperties;
    }

    public static SDKKafkaSender wrap(TraceProps properties, KafkaSender sender) {
        return new SDKKafkaSender(sender, properties);
    }

    public boolean isClose() {
        return kafkaSender.closeCalled;
    }

    public void close() throws IOException {
        kafkaSender.close();
    }

    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        if (!traceProperties.isEnabled()) { // 不发送消息
            return null;
        }
        if (kafkaSender.closeCalled) {
            throw new IllegalStateException("closed");
        } else {
            byte[] message = kafkaSender.encoder.encode(encodedSpans);
            return kafkaSender.new KafkaCall(message);
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

    public CheckResult check() {
        return kafkaSender.check();
    }


    @Override
    public int messageSizeInBytes(int encodedSizeInBytes) {
        return kafkaSender.messageSizeInBytes(encodedSizeInBytes);
    }


}
