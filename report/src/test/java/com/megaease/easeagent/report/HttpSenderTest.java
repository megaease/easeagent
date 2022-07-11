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

package com.megaease.easeagent.report;

import com.megaease.easeagent.report.sender.okhttp.HttpSender;
import lombok.SneakyThrows;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test class can connect to a test server, please update user pwd cert key
 */
public class HttpSenderTest {

    @SneakyThrows
//    @Test
    public void perform() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpSender.appendBasicAuth(builder, user, pwd);
        HttpSender.appendTLS(builder, this.tlsCaCert, this.tlsCert, this.tlsKey);

        String json = "{\"id\":1,\"name\":\"John\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        OkHttpClient client = builder.build();
        Call call = client.newCall(request);
        Response response = call.execute();
        System.out.println(response);
        Assert.assertEquals(200, response.code());
    }

    private String url = "";
    private String user = "";
    private String pwd = "";
    private String tlsKey = "";
    private String tlsCert = "";
    private String tlsCaCert = "";

}
