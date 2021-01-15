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