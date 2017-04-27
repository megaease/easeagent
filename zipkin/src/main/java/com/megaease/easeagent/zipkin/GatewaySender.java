package com.megaease.easeagent.zipkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewaySender.class);

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
            final int code = connection.getResponseCode();
            InputStream in = connection.getInputStream();
            try {
                while (in.read() != -1) ; // skip
            } finally {
                in.close();
            }
            if (code >= 400) throw new IOException(connection.getResponseMessage());
        } catch (IOException e) {
            InputStream err = connection.getErrorStream();
            if (err != null) { // possible, if the connection was dropped
                try {
                    while (err.read() != -1) ; // skip
                } finally {
                    err.close();
                }
            }
            LOGGER.error("Send failed", e);
        }
    }

}
