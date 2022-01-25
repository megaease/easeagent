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

package com.megaease.easeagent.plugin.api.metric;

/**
 * An incrementing and decrementing counter metric.
 */
public interface Counter extends Metric {
    /**
     * Increment the counter by one.
     */
    void inc();

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    void inc(long n);

    /**
     * Decrement the counter by one.
     */
    void dec();

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be decreased
     */
    void dec(long n);

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    long getCount();

    /**
     * Returns the underlying Counter object or {@code null} if there is none. Here is a Counter
     * objects: {@code com.codahale.metrics.Counter}
     *
     * @return
     */
    Object unwrap();
}

