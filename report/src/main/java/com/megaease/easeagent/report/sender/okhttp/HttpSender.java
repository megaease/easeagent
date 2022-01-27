package com.megaease.easeagent.report.sender.okhttp;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.plugin.NoOpCallback;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

import java.io.IOException;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@Slf4j
public class HttpSender implements Sender {
    public static final String SENDER_NAME = "http";

    private static final String URL = join(GENERAL_SENDER, "url");
    private static final String USER_NAME = join(GENERAL_SENDER, "userName");
    private static final String PASSWORD = join(GENERAL_SENDER, "password");
    private static final String GZIP = join(GENERAL_SENDER, "compress");

    private String url;
    private HttpUrl httpUrl;
    private String userName;
    private String password;

    private boolean enabled;
    private boolean gzip;
    private boolean isAuth;

    private OkHttpClient client;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        this.url = config.getString(URL);
        this.userName = config.getString(GENERAL_SENDER);
        this.password = config.getString(PASSWORD);
        this.enabled = NoNull.of(config.getBoolean(PASSWORD), true);
        this.enabled = NoNull.of(config.getBoolean(GZIP), true);

        if (StringUtils.isEmpty(url)) {
            this.enabled = false;
        } else {
            this.httpUrl = HttpUrl.parse(this.url);
            if (this.httpUrl == null) {
                log.error("Invalid Url:{}", this.url);
                this.enabled = false;
            }
        }

        this.isAuth = !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password);
        client = buildClient();
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        if (!enabled) {
            return NoOpCallback.getInstance(Void.class);
        }
        Request request;
        try {
            request = newRequest(new ByteRequestBody(encodedData));
        } catch (IOException e) {
            return NoOpCallback.getInstance(Void.class);
        }
        return new HttpCall(client.newCall(request));
    }

    @Override
    public boolean isAvailable() {
        return this.enabled;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
    }

    @Override
    public void close() throws IOException {
    }

    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (this.isAuth) {
            return builder.authenticator(new Authenticator() {
                @Override public Request authenticate(Route route, Response response) throws IOException {
                    if (response.request().header("Authorization") != null) {
                        return null;
                    }
                    log.info("Authenticating for response: " + response);
                    log.info("Challenges: " + response.challenges());
                    String credential = Credentials.basic(userName, password);
                    return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
                }
            }).build();
        } else {
            return builder.build();
        }
    }

    // borrow form zipkin-reporter
    Request newRequest(RequestBody body) throws IOException {
        Request.Builder request = new Request.Builder().url(httpUrl);
        // Amplification can occur when the Zipkin endpoint is proxied, and the proxy is instrumented.
        // This prevents that in proxies, such as Envoy, that understand B3 single format,
        request.addHeader("b3", "0");
        if (this.gzip) {
            request.addHeader("Content-Encoding", "gzip");
            Buffer gzipped = new Buffer();
            BufferedSink gzipSink = Okio.buffer(new GzipSink(gzipped));
            body.writeTo(gzipSink);
            gzipSink.close();
            body = new BufferRequestBody(body.contentType(), gzipped);
        }
        request.post(body);
        return request.build();
    }

    // from zipkin-reporter-java
    static final class BufferRequestBody extends RequestBody {
        final MediaType contentType;
        final Buffer body;

        BufferRequestBody(MediaType contentType, Buffer body) {
            this.contentType = contentType;
            this.body = body;
        }

        @Override public long contentLength() {
            return body.size();
        }

        @Override public MediaType contentType() {
            return contentType;
        }

        @Override public void writeTo(BufferedSink sink) throws IOException {
            sink.write(body, body.size());
        }
    }
}
