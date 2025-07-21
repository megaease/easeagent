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

package com.megaease.easeagent.plugin.tomcat.interceptor;

import com.megaease.easeagent.plugin.tomcat.utils.ServletUtils;
import com.megaease.easeagent.plugin.tools.trace.TraceConst;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestServletUtils {
    public static MockHttpServletRequest buildMockRequest() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest(TestConst.METHOD, TestConst.URL);
        httpServletRequest.setQueryString(TestConst.QUERY_STRING);
        httpServletRequest.addHeader(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        httpServletRequest.setAttribute(TraceConst.HTTP_ATTRIBUTE_ROUTE, TestConst.ROUTE);
        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, TestConst.ROUTE);
        httpServletRequest.setRemoteAddr(TestConst.REMOTE_ADDR);
        httpServletRequest.setRemotePort(TestConst.REMOTE_PORT);
        return httpServletRequest;
    }

    public static HttpServletResponse buildMockResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setBufferSize(TestConst.RESPONSE_BUFFER_SIZE);
        response.setStatus(200);
        return response;
    }
}
