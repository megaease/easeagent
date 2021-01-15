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

 package com.megaease.easeagent.zipkin;

import zipkin.reporter.BytesMessageEncoder;
import zipkin.reporter.Callback;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPOutputStream;

class GatewaySender implements Sender {

    private final Encoding encoding;
    private final int messageMaxBytes;
    private final String sendEndpoint;
    private final int connectTimeout;
    private final int readTimeout;
    private final boolean sendCompression;
    private final String userAgent;

    /**
     * close is typically called from a different thread
     */
    private volatile boolean closeCalled;


    GatewaySender(int messageMaxBytes, String sendEndpoint, int connectTimeout, int readTimeout,
                  boolean sendCompression, String userAgent) {
        this.messageMaxBytes = messageMaxBytes;
        this.sendEndpoint = sendEndpoint;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.sendCompression = sendCompression;
        this.userAgent = userAgent;
        encoding = Encoding.JSON;
    }

    @Override
    public Encoding encoding() {
        return encoding;
    }

    @Override
    public int messageMaxBytes() {
        return messageMaxBytes;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encoding().listSizeInBytes(encodedSpans);
    }

    @Override
    public void sendSpans(List<byte[]> encodedSpans, Callback callback) {
        if (closeCalled) throw new IllegalStateException("close");
        try {
            byte[] message = BytesMessageEncoder.JSON.encode(encodedSpans);
            send(message, "application/json");
            callback.onComplete();
        } catch (Throwable e) {
            callback.onError(e);
            if (e instanceof Error) throw (Error) e;
        }
    }

    @Override
    public CheckResult check() {
        try {
            send(new byte[]{'[', ']'}, "application/json");
            return CheckResult.OK;
        } catch (Exception e) {
            return CheckResult.failed(e);
        }

    }

    @Override
    public void close() throws IOException {
        closeCalled = true;
    }

    private void send(byte[] body, String mediaType) throws IOException {
        // intentionally not closing the connection, so as to use keep-alives
        HttpURLConnection connection = (HttpURLConnection) new URL(sendEndpoint).openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", mediaType);
        connection.addRequestProperty("User-Agent", userAgent);
        if (sendCompression) {
            connection.addRequestProperty("Content-Encoding", "gzip");
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            GZIPOutputStream compressor = new GZIPOutputStream(gzipped);
            try {
                compressor.write(body);
            } finally {
                compressor.close();
            }
            body = gzipped.toByteArray();
        }
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(body.length);
        connection.getOutputStream().write(body);

        try {
            discard(connection.getInputStream());
            final int code = connection.getResponseCode();
            if (code >= 400) throw new IOException(connection.getResponseMessage());
        } finally {
            InputStream err = connection.getErrorStream();
            if (err != null) { // possible, if the connection was dropped
                discard(err);
            }
        }
    }

    private void discard(InputStream in) throws IOException {
        try {
            while (in.read() != -1) ; // skip
        } finally {
            in.close();
        }
    }

}
