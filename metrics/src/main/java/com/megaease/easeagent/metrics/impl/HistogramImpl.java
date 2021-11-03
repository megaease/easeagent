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

package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Histogram;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;

public class HistogramImpl implements com.megaease.easeagent.plugin.api.metric.Histogram {
    private final Histogram histogram;

    private HistogramImpl(@Nonnull Histogram histogram) {
        this.histogram = histogram;
    }

    public static com.megaease.easeagent.plugin.api.metric.Histogram build(Histogram histogram){
        return histogram==null?NoOpMetrics.NO_OP_HISTOGRAM:new HistogramImpl(histogram);
    }

    @Override
    public void update(int value) {
        histogram.update(value);
    }

    @Override
    public void update(long value) {
        histogram.update(value);
    }

    @Override
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    public Snapshot getSnapshot() {
        return SnapshotImpl.build(histogram.getSnapshot());
    }
}
