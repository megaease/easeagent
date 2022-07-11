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
package com.megaease.easeagent.report.async.zipkin;

/*
 * Copyright 2016-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.megaease.easeagent.plugin.report.Encoder;

import java.util.ArrayList;
import java.util.Iterator;

/** Use of this type happens off the application's main thread. This type is not thread-safe */
@SuppressWarnings("unused")
public class AgentBufferNextMessage<S> implements WithSizeConsumer<S> {
    public static <S> AgentBufferNextMessage<S> create(Encoder<S> encoder, int maxBytes, long timeoutNanos) {
        return new AgentBufferNextMessage<>(encoder, maxBytes, timeoutNanos);
    }

    final Encoder<S> encoder;
    final int maxBytes;
    final long timeoutNanos;
    final ArrayList<S> spans = new ArrayList<>();
    final ArrayList<Integer> sizes = new ArrayList<>();

    long deadlineNanoTime;
    int packageSizeInBytes;
    boolean bufferFull;

    AgentBufferNextMessage(Encoder<S> coder, int maxBytes, long timeoutNanos) {
        this.maxBytes = maxBytes;
        this.timeoutNanos = timeoutNanos;
        this.encoder = coder;
        resetMessageSizeInBytes();
    }

    int messageSizeInBytes(int nextSizeInBytes) {
        return packageSizeInBytes + encoder.appendSizeInBytes(nextSizeInBytes);
    }

    void resetMessageSizeInBytes() {
        packageSizeInBytes = encoder.packageSizeInBytes(sizes);
    }

    /** This is done inside a lock that holds up writers, so has to be fast. No encoding! */
    public boolean offer(S next, int nextSizeInBytes) {
        int x = messageSizeInBytes(nextSizeInBytes);
        int includingNextVsMaxBytes = Integer.compare(x, maxBytes); // Integer.compare, but JRE 6

        if (includingNextVsMaxBytes > 0) {
            bufferFull = true;
            return false; // can't fit the next message into this buffer
        }

        addSpanToBuffer(next, nextSizeInBytes);
        packageSizeInBytes = x;

        if (includingNextVsMaxBytes == 0) bufferFull = true;
        return true;
    }

    void addSpanToBuffer(S next, int nextSizeInBytes) {
        spans.add(next);
        sizes.add(nextSizeInBytes);
    }

    public long remainingNanos() {
        if (spans.isEmpty()) {
            deadlineNanoTime = System.nanoTime() + timeoutNanos;
        }
        return Math.max(deadlineNanoTime - System.nanoTime(), 0);
    }

    public boolean isReady() {
        return bufferFull || remainingNanos() <= 0;
    }

    // this occurs off the application thread
    public void drain(WithSizeConsumer<S> consumer) {
        Iterator<S> spanIterator = spans.iterator();
        Iterator<Integer> sizeIterator = sizes.iterator();
        while (spanIterator.hasNext()) {
            if (consumer.offer(spanIterator.next(), sizeIterator.next())) {
                bufferFull = false;
                spanIterator.remove();
                sizeIterator.remove();
            } else {
                break;
            }
        }

        resetMessageSizeInBytes();
        // regardless, reset the clock
        deadlineNanoTime = 0;
    }

    public int count() {
        return spans.size();
    }

    public int sizeInBytes() {
        return packageSizeInBytes;
    }
}
