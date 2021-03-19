package com.megaease.easeagent.common;

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
        final int size = 2;
        CountDownLatch countDownLatch = new CountDownLatch(size);
        final ThreadLocalCurrentContext currentContext = ThreadLocalCurrentContext.DEFAULT;
        try (final ThreadLocalCurrentContext.Scope scope1 = currentContext.newScope(ThreadLocalCurrentContext.createContext("hello", "world"))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertNull(currentContext.get());
                    countDownLatch.countDown();
                }
            }).start();
            new Thread(currentContext.wrap(new Runnable() {
                @Override
                public void run() {
                    assertEquals("world", currentContext.get().get("hello"));
                    countDownLatch.countDown();
                }
            })).start();
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
