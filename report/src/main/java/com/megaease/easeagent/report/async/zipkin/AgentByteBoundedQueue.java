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
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

/**
 * Multi-producer, multi-consumer queue that is bounded by both count and size.
 *
 * <p>
 * This queue is implemented based on LinkedTransferQueue and implements the maximum number and the maximum number of bytes
 * on the basis of LinkedTransferQueue. Taking advantage of the lock-free performance of LinkedTransferQueue in
 * inserting data, the performance problem of locking in the old version can be avoided.
 * </p>
 */
public final class AgentByteBoundedQueue<S> implements WithSizeConsumer<S> {

    private final LinkedTransferQueue<DataWrapper<S>> queue = new LinkedTransferQueue<>();

    private final AtomicInteger sizeInBytes = new AtomicInteger(0);

    private final int maxSize;

    private final int maxBytes;

    private final LongAdder loseCounter = new LongAdder();

    public AgentByteBoundedQueue(int maxSize, int maxBytes) {
        this.maxSize = maxSize;
        this.maxBytes = maxBytes;
    }

    @Override
    public boolean offer(S next, int nextSizeInBytes) {
        if (maxSize == queue.size()) {
            loseCounter.increment();
            return false;
        }
        if (sizeInBytes.updateAndGet(pre -> pre + nextSizeInBytes) > maxBytes) {
            loseCounter.increment();
            sizeInBytes.updateAndGet(pre -> pre - nextSizeInBytes);
            return false;
        }
        queue.offer(new DataWrapper<>(next, nextSizeInBytes));
        return true;
    }

    int doDrain(WithSizeConsumer<S> consumer, DataWrapper<S> firstPoll) {
        int drainedCount = 0;
        int drainedSizeInBytes = 0;
        DataWrapper<S> next = firstPoll;
        do {
            int nextSizeInBytes = next.getSizeInBytes();
            if (consumer.offer(next.getElement(), nextSizeInBytes)) {
                drainedCount++;
                drainedSizeInBytes += nextSizeInBytes;
            } else {
                queue.offer(next);
                break;
            }
        } while ((next = queue.poll()) != null);
        final int updateValue = drainedSizeInBytes;
        sizeInBytes.updateAndGet(pre -> pre - updateValue);
        return drainedCount;
    }

    public int drainTo(WithSizeConsumer<S> consumer, long nanosTimeout) {
        DataWrapper<S> firstPoll;
        try {
            firstPoll = queue.poll(nanosTimeout, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            return 0;
        }
        if (firstPoll == null) {
            return 0;
        }
        return doDrain(consumer, firstPoll);
    }

    public int getCount() {
        return queue.size();
    }

    public int getSizeInBytes() {
        return sizeInBytes.get();
    }

    public int clear() {
        DataWrapper<S> data;
        int result = 0;
        int removeBytes = 0;
        while ((data = queue.poll()) != null) {
            removeBytes += data.getSizeInBytes();
            result++;
        }
        sizeInBytes.addAndGet(removeBytes * -1);
        return result;
    }

    public long getLoseCount() {
        return loseCounter.longValue();
    }

    @Data
    private static class DataWrapper<S> {

        private final S element;

        private final int sizeInBytes;
    }

}

