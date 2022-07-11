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

package org.springframework.web.reactive.function.client;

import org.springframework.mock.http.client.reactive.MockClientHttpResponse;

public class MockDefaultClientResponse {
    public static Builder builder() {
        return new Builder(200);
    }

    public static Builder builder(int status) {
        return new Builder(status);
    }

    public static class Builder {
        MockClientHttpResponse mockClientHttpResponse;

        public Builder(int status) {
            mockClientHttpResponse = new MockClientHttpResponse(status);
        }

        public Builder addHeader(String name, String value) {
            mockClientHttpResponse.getHeaders().add(name, value);
            return this;
        }

        public DefaultClientResponse build() {
            return new DefaultClientResponse(mockClientHttpResponse, null, "", "", null);
        }
    }
}
