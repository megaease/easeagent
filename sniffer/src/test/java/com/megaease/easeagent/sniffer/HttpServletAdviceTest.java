/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.sniffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpServletAdviceTest extends BaseSnifferTest {

//    @Test
//    public void success() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        final Definition.Default def = new GenHttpServletAdvice().define(Definition.Default.EMPTY);
//        String baseName = HttpServletAdviceTest.class.getName();
//        final ClassLoader loader = new URLClassLoader(new URL[0]);
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        final HttpServlet httpServlet = (HttpServlet) Classes.transform(baseName + "$MyHttpServlet")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4Filter", builder))
//                .load(loader).get(0).newInstance();
//
//        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
//        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
//        httpServlet.service(httpServletRequest, httpServletResponse);
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }

    public static class MyHttpServlet extends HttpServlet {

        public MyHttpServlet() {
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // do nothing
        }
    }
}
