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

import lombok.Data;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntUnaryOperator;

/**
 * Multi-producer, multi-consumer queue that is bounded by both count and size.
 *
 * <p>This is similar to {@link java.util.concurrent.ArrayBlockingQueue} in implementation.
 */
public final class AgentByteBoundedQueue<S> implements WithSizeConsumer<S> {

    private final AtomicReference<State<S>> state = new AtomicReference<>(new State<>());

    private final int maxSize;

    private final int maxBytes;

    private final LongAdder loseCounter = new LongAdder();

    public AgentByteBoundedQueue(int maxSize, int maxBytes) {
        this.maxSize = maxSize;
        this.maxBytes = maxBytes;
    }

    @Override
    public boolean offer(S next, int nextSizeInBytes) {
        State<S> sState = state.get();
        if (maxSize == sState.getQueueSize()) {
            loseCounter.increment();
            return false;
        }
        if (sState.updateAndGet(pre -> pre + nextSizeInBytes) > maxBytes) {
            loseCounter.increment();
            sState.updateAndGet(pre -> pre - nextSizeInBytes);
            return false;
        }
        sState.offer(new DataWrapper<>(next, nextSizeInBytes));
        return true;
    }

    int doDrain(WithSizeConsumer<S> consumer, DataWrapper<S> firstPoll) {
        State<S> sState = state.get();
        int drainedCount = 0;
        int drainedSizeInBytes = 0;
        DataWrapper<S> next = null;
        while (drainedCount < sState.getQueueSize()) {
            if (next == null) {
                next = firstPoll;
            } else {
                next = sState.poll();
            }
            if (next == null) break;
            int nextSizeInBytes = next.getSizeInBytes();
            if (consumer.offer(next.getElement(), nextSizeInBytes)) {
                drainedCount++;
                drainedSizeInBytes += nextSizeInBytes;
            } else {
                break;
            }
        }
        final int updateValue = drainedSizeInBytes;
        sState.updateAndGet(pre -> pre - updateValue);
        return drainedCount;
    }

    public int drainTo(WithSizeConsumer<S> consumer, long nanosTimeout) {
        State<S> sState = state.get();
        DataWrapper<S> firstPoll;
        try {
            firstPoll = sState.poll(nanosTimeout, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            return 0;
        }
        if (firstPoll == null) {
            return 0;
        }
        return doDrain(consumer, firstPoll);
    }

    public int getCount() {
        return state.get().getQueueSize();
    }

    public int getSizeInBytes() {
        return state.get().getSizeInBytes();
    }

    public int clear() {
        return state.getAndSet(new State<>()).getQueueSize();
    }

    public long getLoseCount() {
        return loseCounter.longValue();
    }

    @Data
    private static class DataWrapper<S> {

        private final S element;

        private final int sizeInBytes;
    }

    private static class State<S> {

        private final LinkedTransferQueue<DataWrapper<S>> queue = new LinkedTransferQueue<>();

        private final AtomicInteger sizeInBytes = new AtomicInteger(0);

        public int getQueueSize() {
            return queue.size();
        }

        public DataWrapper<S> poll() {
            return queue.poll();
        }

        public boolean offer(DataWrapper<S> data) {
            return queue.offer(data);
        }

        public DataWrapper<S> poll(long nanosTimeout, TimeUnit unit) throws InterruptedException {
            return queue.poll(nanosTimeout, unit);
        }

        public long updateAndGet(IntUnaryOperator updateFunction) {
            return sizeInBytes.updateAndGet(updateFunction);
        }

        public int getSizeInBytes() {
            return sizeInBytes.get();
        }

    }

}

