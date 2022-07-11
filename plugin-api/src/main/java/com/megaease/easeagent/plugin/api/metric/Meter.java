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
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * moving average throughput.
 */
public interface Meter extends Metric {

    /**
     * Mark the occurrence of an event.
     */
    void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    void mark(long n);

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    long getCount();

    /**
     * Returns the fifteen-minute moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the fifteen-minute moving average rate at which events have
     * occurred since the meter was created
     */
    double getFifteenMinuteRate();

    /**
     * Returns the five-minute moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the five-minute moving average rate at which events have
     * occurred since the meter was created
     */
    double getFiveMinuteRate();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */

    double getMeanRate();

    /**
     * Returns the one-minute moving average rate at which events have
     * occurred since the meter was created.
     *
     * @return the one-minute moving average rate at which events have
     * occurred since the meter was created
     */
    double getOneMinuteRate();

    /**
     * Returns the underlying Meter object or {@code null} if there is none. Here is a Meter
     * objects: {@code com.codahale.metrics.Meter}
     *
     * @return
     */
    Object unwrap();
}

