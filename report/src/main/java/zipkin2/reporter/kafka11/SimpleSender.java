package zipkin2.reporter.kafka11;

import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.codec.Encoding;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;

import java.io.IOException;
import java.util.List;

public class SimpleSender extends Sender implements SDKSender {
    private BytesMessageEncoder encoder;

    public SimpleSender() {
        encoder = BytesMessageEncoder.forEncoding(Encoding.JSON);
    }

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        return 1000000;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return Encoding.JSON.listSizeInBytes(encodedSpans);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        return new Call.Base<Void>() {
            Mapper mapper;
            Call delegate;

            @Override
            public Call<Void> clone() {
                return null;
            }

            @Override
            protected Void doExecute() throws IOException {
                return (Void) this.mapper.map(this.delegate.execute());
            }

            @Override
            protected void doEnqueue(Callback<Void> callback) {
                this.delegate.enqueue(new Callback<Void>() {
                    public void onSuccess(Void value) {
                        try {
                            callback.onSuccess((Void) mapper.map(value));
                        } catch (Throwable var3) {
                            callback.onError(var3);
                        }
                    }

                    public void onError(Throwable t) {
                        callback.onError(t);
                    }
                });
            }
        };
    }

    @Override
    public boolean isClose() {
        return true;
    }
}
