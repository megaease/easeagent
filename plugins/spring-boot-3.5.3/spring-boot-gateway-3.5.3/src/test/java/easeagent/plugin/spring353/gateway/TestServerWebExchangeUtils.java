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

package easeagent.plugin.spring353.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.net.InetSocketAddress;

public class TestServerWebExchangeUtils {

    public static final MockServerHttpRequest.BaseBuilder<?> builder() {
        return MockServerHttpRequest.get("http://192.168.0.12:8080/test", "a=b")
            .header(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE)
            .queryParam("a", "b");
    }

    public static final MockServerWebExchange build(MockServerHttpRequest.BaseBuilder<?> requestBuilder) {
        MockServerHttpRequest mockServerHttpRequest = requestBuilder.build();
        return MockServerWebExchange.builder(mockServerHttpRequest).build();
    }


    public static final MockServerWebExchange mockServerWebExchange() {
        MockServerHttpRequest mockServerHttpRequest = TestServerWebExchangeUtils.builder()
            .remoteAddress(new InetSocketAddress("192.168.0.12", 8080)).build();
        MockServerWebExchange mockServerWebExchange = MockServerWebExchange.builder(mockServerHttpRequest).build();
        mockServerWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        PathPatternParser parser = new PathPatternParser();
        PathPattern pathPattern = parser.parse("/test");
        mockServerWebExchange.getAttributes().put(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, pathPattern);
        return mockServerWebExchange;
    }
}
