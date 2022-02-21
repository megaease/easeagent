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

package easeagent.plugin.spring.gateway.interceptor.initialize;

import org.junit.Test;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.concurrent.atomic.AtomicBoolean;

import static easeagent.plugin.spring.gateway.TestServerWebExchangeUtils.mockServerWebExchange;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AgentGlobalFilterTest {

    @Test
    public void filter() {
        AgentGlobalFilter agentGlobalFilter = new AgentGlobalFilter();
        AtomicBoolean ran = new AtomicBoolean(true);
        MockServerWebExchange mockServerWebExchange = mockServerWebExchange();
        agentGlobalFilter.filter(mockServerWebExchange, exchange -> {
            assertSame(mockServerWebExchange, exchange);
            ran.set(true);
            return null;
        });
        assertTrue(ran.get());
    }


}
