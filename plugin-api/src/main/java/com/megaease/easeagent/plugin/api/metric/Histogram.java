/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.api.metric;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 * variance</a>
 */
public interface Histogram extends Metric {
    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(int value);

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    void update(long value);

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    long getCount();

    /**
     * Returns a snapshot of the values.
     *
     * @return a snapshot of the values
     */
    Snapshot getSnapshot();

    /**
     * Returns the underlying Histogram object or {@code null} if there is none. Here is a Histogram
     * objects: {@code com.codahale.metrics.Histogram}
     *
     * @return
     */
    Object unwrap();
}
