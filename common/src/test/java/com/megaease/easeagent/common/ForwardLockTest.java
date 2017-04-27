package com.megaease.easeagent.common;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ForwardLockTest {
    @Test
    public void should_work() throws Exception {
        final ForwardLock detector = new ForwardLock();
        final List<Boolean> bools = Lists.newArrayList();

        final ForwardLock.Supplier<Boolean> supplier = new ForwardLock.Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return true;
            }
        };


        final ForwardLock.Consumer<Boolean> consumer = new ForwardLock.Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                bools.add(aBoolean);
            }
        };

        final ForwardLock.Release<Boolean> release = detector.acquire(supplier);
        detector.acquire(supplier).apply(consumer);
        release.apply(consumer);

        assertThat(bools.size(), is(1));
        assertTrue(bools.get(0));
    }
}