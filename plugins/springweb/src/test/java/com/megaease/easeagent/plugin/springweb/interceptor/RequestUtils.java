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

package com.megaease.easeagent.plugin.springweb.interceptor;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;

import java.util.Arrays;
import java.util.Collections;

public class RequestUtils {
    public static final String URL = "http://127.0.0.1:8080";

    public static Request buildFeignClient() {
        RequestTemplate requestTemplate = new RequestTemplate();
        Request request = Request.create(
            Request.HttpMethod.GET,
            URL,
            requestTemplate.headers(),
            Request.Body.create(requestTemplate.body()),
            requestTemplate
        );
        return request;
    }

    public static Response.Builder responseBuilder(Request request) {
        Response.Builder builder = Response.builder();
        builder.status(200);
        builder.request(request);
        builder.headers(Collections.singletonMap(TestConst.RESPONSE_TAG_NAME, Arrays.asList(TestConst.RESPONSE_TAG_VALUE)));
        builder.body("test".getBytes());
        return builder;
    }
}
