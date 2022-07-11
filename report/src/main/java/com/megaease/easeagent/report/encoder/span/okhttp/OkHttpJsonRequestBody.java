/*
 * Copyright (c) 2022, MegaEase
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
package com.megaease.easeagent.report.encoder.span.okhttp;

import com.megaease.easeagent.plugin.report.EncodedData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OkHttpJsonRequestBody extends RequestBody implements EncodedData {
    static final MediaType CONTENT_TYPE = MediaType.parse("application/json");

    private final byte[] data;
    private final int contentLength;

    @Override
    public int size() {
        return this.contentLength;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    public OkHttpJsonRequestBody(byte[] data) {
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
