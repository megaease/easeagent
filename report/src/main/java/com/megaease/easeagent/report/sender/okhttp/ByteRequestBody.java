package com.megaease.easeagent.report.sender.okhttp;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteRequestBody extends RequestBody {
    // for improved: this should given by encoder
    static final MediaType CONTENT_TYPE = MediaType.parse("application/json");

    private final byte[] data;
    private final int contentLength;

    public ByteRequestBody(byte[] data) {
        this.data = data;
        this.contentLength = data.length;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        sink.write(data);
    }

    @Override public long contentLength() {
        return contentLength;
    }
}
