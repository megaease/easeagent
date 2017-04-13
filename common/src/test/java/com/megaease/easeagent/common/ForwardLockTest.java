package com.megaease.easeagent.common;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ForwardLockTest {
    @Test
    public void should_work() throws Exception {
        final ForwardLock detector = new ForwardLock();
        final Object first = new Object();
        final Object second = new Object();

        assertTrue(detector.acquire(first));
        assertFalse(detector.acquire(second));
        assertFalse(detector.release(second));
        assertTrue(detector.release(first));

        assertTrue(detector.acquire(second));
        assertTrue(detector.release(second));
    }
}