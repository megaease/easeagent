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

package easeagent.plugin.spring.gateway.interceptor.metric;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(EaseAgentJunit4ClassRunner.class)
public class TimeUtilsTest {

    @Test
    public void startTime() throws InterruptedException {
        Object key = new Object();
        long startTime = TimeUtils.startTime(EaseAgent.getContext(), key);
        Thread.sleep(10);
        assertEquals(startTime, TimeUtils.startTime(EaseAgent.getContext(), key));
        Object key2 = new Object();
        assertNotEquals(startTime, TimeUtils.startTime(EaseAgent.getContext(), key2));
    }

    @Test
    public void removeStartTime() throws InterruptedException {
        Object key = new Object();
        long startTime = TimeUtils.startTime(EaseAgent.getContext(), key);
        Long startObj = TimeUtils.removeStartTime(EaseAgent.getContext(), key);
        assertNotNull(startObj);
        assertEquals(startTime, (long) startObj);
        Thread.sleep(10);
        assertNotEquals(startTime, TimeUtils.startTime(EaseAgent.getContext(), key));
    }
}
