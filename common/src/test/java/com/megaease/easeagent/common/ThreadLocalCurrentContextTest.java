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

package com.megaease.easeagent.common;

import com.megaease.easeagent.core.utils.ThreadLocalCurrentContext;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ThreadLocalCurrentContextTest {
    @Test
    public void should_work() throws Exception {
        final ThreadLocalCurrentContext currentContext = ThreadLocalCurrentContext.DEFAULT;
        try (final ThreadLocalCurrentContext.Scope scope1 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", "world"))) {
            try (final ThreadLocalCurrentContext.Scope scope2 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", "internal"))) {
                assertEquals("internal", currentContext.get().get("hello"));
            }
            assertEquals("world", currentContext.get().get("hello"));
        }
    }

    @Test
    public void should_work_multiple() throws Exception {
        final int size = 1;
        CountDownLatch countDownLatch = new CountDownLatch(size);
        final ThreadLocalCurrentContext currentContext = ThreadLocalCurrentContext.DEFAULT;
        try (final ThreadLocalCurrentContext.Scope scope1 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", "world"))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertEquals("world", currentContext.get().get("hello"));
                    countDownLatch.countDown();
                }
            }).start();
            assertEquals("world", currentContext.get().get("hello"));
        }
        countDownLatch.await();
    }

    @Test
    public void should_work_multiple2() throws Exception {
        final int size = 10;
        CountDownLatch countDownLatch = new CountDownLatch(size);
        final ThreadLocalCurrentContext currentContext = ThreadLocalCurrentContext.DEFAULT;
        try (final ThreadLocalCurrentContext.Scope scope1 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", "world"))) {
            for (int i = 0; i < size; i++) {
                int finalI = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String value = "internal" + finalI;
                        try (final ThreadLocalCurrentContext.Scope scope2 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", value))) {
                            assertEquals(value, currentContext.get().get("hello"));
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {

                        }
                        countDownLatch.countDown();
                    }
                }).start();
            }
            assertEquals("world", currentContext.get().get("hello"));
        }
        countDownLatch.await();
    }

}
