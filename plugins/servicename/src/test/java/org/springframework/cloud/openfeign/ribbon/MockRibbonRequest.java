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

package org.springframework.cloud.openfeign.ribbon;

import feign.Request;
import feign.RequestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class MockRibbonRequest {
    public static FeignLoadBalancer.RibbonRequest createRibbonRequest() throws URISyntaxException {
        RequestTemplate requestTemplate = new RequestTemplate();
        String uri = "http://127.0.0.1:8080";
        Request request = Request.create(
            Request.HttpMethod.GET,
            uri,
            requestTemplate.headers(),
            Request.Body.create(requestTemplate.body()),
            requestTemplate
        );
        return new FeignLoadBalancer.RibbonRequest(null, request, new URI(uri));
    }

    public static Request getRequest(Object ribbonRequest) {
        FeignLoadBalancer.RibbonRequest request = (FeignLoadBalancer.RibbonRequest) ribbonRequest;
        return request.getRequest();
    }
}
