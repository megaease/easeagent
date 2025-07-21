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

package org.springframework.http.client;


import com.megaease.easeagent.plugin.rest.template.interceptor.TestConst;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleClientHttpResponseFactory {
    public static ClientHttpResponse createMockResponse(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection httpURLConnection = new HttpURLConnection(url) {
            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() throws IOException {

            }

            @Override
            public int getResponseCode() throws IOException {
                return 200;
            }

            @Override
            public String getHeaderFieldKey(int n) {
                if (n == 0) {
                    return TestConst.RESPONSE_TAG_NAME;
                }
                return super.getHeaderFieldKey(n);
            }

            @Override
            public String getHeaderField(int n) {
                if (n == 0) {
                    return TestConst.RESPONSE_TAG_VALUE;
                }
                return super.getHeaderField(n);
            }
        };
        return new SimpleClientHttpResponse(httpURLConnection);
    }
}
