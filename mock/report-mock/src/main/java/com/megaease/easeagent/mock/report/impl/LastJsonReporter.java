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

package com.megaease.easeagent.mock.report.impl;

import com.megaease.easeagent.mock.report.JsonReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class LastJsonReporter implements JsonReporter {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicReference<List<Map<String, Object>>> reference = new AtomicReference<>();
    private final Predicate<Map<String, Object>> filter;

    public LastJsonReporter() {
        this(null);
    }

    public LastJsonReporter(Predicate<Map<String, Object>> filter) {
        this.filter = filter;
    }

    @Override
    public void report(List<Map<String, Object>> json) {
        if (filter == null) {
            reference.set(json);
            signalAll();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : json) {
            if (filter.test(stringObjectMap)) {
                result.add(stringObjectMap);
            }
        }
        if (!result.isEmpty()) {
            reference.set(result);
            signalAll();
        }
    }

    private void signalAll() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean wait(long time, TimeUnit unit) {
        lock.lock();
        try {
            try {
                if (!condition.await(time, unit)) {
                    return false;
                }
            } catch (InterruptedException ignore) {
                return false;
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public List<Map<String, Object>> getLast() {
        return reference.get();
    }


    public List<Map<String, Object>> waitOne(long time, TimeUnit unit) {
        List<Map<String, Object>> result = reference.get();
        if (result != null) {
            return result;
        }
        if (!wait(time, unit)) {
            return null;
        }
        return reference.get();
    }


    public void clean() {
        reference.set(null);
    }
}
