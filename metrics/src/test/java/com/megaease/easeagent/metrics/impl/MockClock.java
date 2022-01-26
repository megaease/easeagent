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

package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Clock;

import java.util.Iterator;
import java.util.List;

public class MockClock extends Clock {
    private final Iterator<Long> ticks;

    public MockClock(List<Long> ticks) {
        this.ticks = ticks.iterator();
    }

    @Override
    public long getTick() {
        return ticks.next();
    }
}
