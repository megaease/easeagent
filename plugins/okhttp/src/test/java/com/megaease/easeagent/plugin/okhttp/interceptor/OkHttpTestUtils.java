/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.okhttp.interceptor;

import okhttp3.*;

public class OkHttpTestUtils {
    public static final String URL = "http://127.0.0.1:8080/test";

    public static Call buildCall() {
        Request okRequest = new Request.Builder()
            .url(URL)
            .build();
        OkHttpClient client = new OkHttpClient();
        return client.newCall(okRequest);
    }

    public static Response.Builder responseBuilder(Call call) {
        Response.Builder builder = new Response.Builder();
        builder.code(200);
        builder.request(call.request());
        builder.protocol(Protocol.HTTP_2);
        builder.addHeader("aa", "bb");
        builder.message("test");
        return builder;
    }
}
