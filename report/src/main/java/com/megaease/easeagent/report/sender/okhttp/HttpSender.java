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
package com.megaease.easeagent.report.sender.okhttp;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.async.AgentThreadFactory;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.plugin.NoOpCall;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@AutoService(Sender.class)
public class HttpSender implements Sender {
    public static final String SENDER_NAME = ZIPKIN_SENDER_NAME;

    private static final String AUTH_HEADER = "Authorization";

    private static final String SERVER_USER_NAME_KEY = join(OUTPUT_SERVER_V2, "userName");
    private static final String SERVER_PASSWORD_KEY = join(OUTPUT_SERVER_V2, "password");
    private static final String SERVER_GZIP_KEY = join(OUTPUT_SERVER_V2, "compress");

    private static final String URL_KEY = join(GENERAL_SENDER, "url");
    private static final String USER_NAME_KEY = join(GENERAL_SENDER, "userName");
    private static final String PASSWORD_KEY = join(GENERAL_SENDER, "password");
    private static final String GZIP_KEY = join(GENERAL_SENDER, "compress");
    private static final String MAX_REQUESTS_KEY = join(GENERAL_SENDER, "maxRequests");
    private static final int MIN_TIMEOUT = 30_000;

    private Config config;

    private String url;
    private HttpUrl httpUrl;
    private String userName;
    private String password;

    private boolean enabled;
    private boolean gzip;
    private boolean isAuth;

    private int timeout;
    private int maxRequests;

    private String credential;
    private OkHttpClient client;

    // URL-USER-PASSWORD as unique key shared a client
    static ConcurrentHashMap<String, OkHttpClient> clientMap = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        extractConfig(config);
        this.config = config;
        initClient();
    }

    private void extractConfig(Config config) {
        this.url = getUrl(config);
        this.userName = StringUtils.noEmptyOf(config.getString(USER_NAME_KEY), config.getString(SERVER_USER_NAME_KEY));
        this.password = StringUtils.noEmptyOf(config.getString(PASSWORD_KEY), config.getString(SERVER_PASSWORD_KEY));

        this.gzip = NoNull.of(config.getBooleanNullForUnset(GZIP_KEY),
            NoNull.of(config.getBooleanNullForUnset(SERVER_GZIP_KEY), true));

        this.timeout = NoNull.of(config.getInt(OUTPUT_SERVERS_TIMEOUT), MIN_TIMEOUT);
        if (this.timeout < MIN_TIMEOUT) {
            this.timeout = MIN_TIMEOUT;
        }
        this.enabled = NoNull.of(config.getBooleanNullForUnset(GENERAL_SENDER_ENABLED), true);
        this.maxRequests = NoNull.of(config.getInt(MAX_REQUESTS_KEY), 65);

        if (StringUtils.isEmpty(url) || Boolean.FALSE.equals(config.getBoolean(OUTPUT_SERVERS_ENABLE))) {
            this.enabled = false;
        } else {
            this.httpUrl = HttpUrl.parse(this.url);
            if (this.httpUrl == null) {
                log.error("Invalid Url:{}", this.url);
                this.enabled = false;
            }
        }

        this.isAuth = !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password);
        if (isAuth) {
            this.credential = Credentials.basic(userName, password);
        }
    }

    private String getUrl(Config config) {
        // url
        String outputServer = config.getString(BOOTSTRAP_SERVERS);
        String cUrl = NoNull.of(config.getString(URL_KEY), "");
        if (!StringUtils.isEmpty(outputServer) && !cUrl.startsWith("http")) {
            cUrl = outputServer + cUrl;
        }
        return cUrl;
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        if (!enabled) {
            return NoOpCall.getInstance(Void.class);
        }
        Request request;

        try {
            if (encodedData instanceof RequestBody) {
                request = newRequest((RequestBody)encodedData);
            } else {
                request = newRequest(new ByteRequestBody(encodedData.getData()));
            }
        } catch (IOException e) {
            return NoOpCall.getInstance(Void.class);
        }

        return new HttpCall(client.newCall(request));
    }

    @Override
    public boolean isAvailable() {
        return this.enabled;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        this.config.updateConfigsNotNotify(changes);

        // check new client
        boolean renewClient = !getUrl(this.config).equals(this.url)
            || !this.config.getString(USER_NAME_KEY).equals(this.userName)
            || !this.config.getString(PASSWORD_KEY).equals(this.password);

        if (renewClient) {
            clearClient();
            extractConfig(this.config);
            newClient();
        }
    }

    @Override
    public void close() throws IOException {
        clearClient();
    }

    /** Waits up to a second for in-flight requests to finish before cancelling them */
    private void clearClient() {
        OkHttpClient dClient = clientMap.remove(getClientKey());
        if (dClient == null) {
            return;
        }
        Dispatcher dispatcher = dClient.dispatcher();
        dispatcher.executorService().shutdown();
        try {
            if (!dispatcher.executorService().awaitTermination(1, TimeUnit.SECONDS)) {
                dispatcher.cancelAll();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // different url for different business, so create separate clients with different dispatcher
    private String getClientKey() {
        return this.url + ":" + this.userName + ":" + this.password;
    }

    private void newClient() {
        String clientKey = getClientKey();
        OkHttpClient newClient = clientMap.get(clientKey);
        if (newClient != null) {
            client = newClient;
            return;
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // timeout
        builder.connectTimeout(timeout, MILLISECONDS);
        builder.readTimeout(timeout, MILLISECONDS);
        builder.writeTimeout(timeout, MILLISECONDS);

        // auth
        if (this.isAuth) {
            builder.authenticator((route, response) -> {
                if (response.request().header(AUTH_HEADER) != null) {
                    return null;
                }
                log.info("Authenticating for response: " + response);
                log.info("Challenges: " + response.challenges());
                credential = Credentials.basic(userName, password);
                return response.request().newBuilder()
                    .header(AUTH_HEADER, credential)
                    .build();
            });
        }
        synchronized (HttpSender.class) {
            if (clientMap.get(clientKey) != null) {
                client = clientMap.get(clientKey);
            } else {
                builder.dispatcher(newDispatcher(maxRequests));
                newClient = builder.build();
                clientMap.putIfAbsent(clientKey, newClient);
                client = newClient;
            }
        }
    }

    private void initClient() {
        if (client != null) {
            return;
        }
        newClient();
    }

    // borrow form zipkin-reporter
    private Request newRequest(RequestBody body) throws IOException {
        Request.Builder request = new Request.Builder().url(httpUrl);
        // Amplification can occur when the Zipkin endpoint is accessed through a proxy, and the proxy is instrumented.
        // This prevents that in proxies, such as Envoy, that understand B3 single format,
        request.addHeader("b3", "0");
        if (this.isAuth) {
            request.header(AUTH_HEADER, credential);
        }
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

    static Dispatcher newDispatcher(int maxRequests) {
        // bound the executor so that we get consistent performance
        ThreadPoolExecutor dispatchExecutor =
            new ThreadPoolExecutor(0, maxRequests, 60, TimeUnit.SECONDS,
                // Using a synchronous queue means messages will send immediately until we hit max
                // in-flight requests. Once max requests are hit, send will block the caller, which is
                // the AsyncReporter flush thread. This is ok, as the AsyncReporter has a buffer of
                // unsent spans for this purpose.
                new SynchronousQueue<>(),
                OkHttpSenderThreadFactory.INSTANCE);

        Dispatcher dispatcher = new Dispatcher(dispatchExecutor);
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequests);
        return dispatcher;
    }

    static class OkHttpSenderThreadFactory extends AgentThreadFactory {
        public static final OkHttpSenderThreadFactory INSTANCE = new OkHttpSenderThreadFactory();
        @Override public Thread newThread(Runnable r) {
            return new Thread(r, "AgentHttpSenderDispatcher-" + createCount.getAndIncrement());
        }
    }

    // from zipkin-reporter-java
    static final class BufferRequestBody extends RequestBody {
        final MediaType contentType;
        final Buffer body;

        BufferRequestBody(MediaType contentType, Buffer body) {
            this.contentType = contentType;
            this.body = body;
        }

        @Override
        public long contentLength() {
            return body.size();
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(body, body.size());
        }
    }
}
