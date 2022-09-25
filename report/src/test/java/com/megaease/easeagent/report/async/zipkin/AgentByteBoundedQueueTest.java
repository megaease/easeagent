package com.megaease.easeagent.report.async.zipkin;

import com.megaease.easeagent.report.plugin.NoOpEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentByteBoundedQueueTest {

    private AgentByteBoundedQueue<String> queue = new AgentByteBoundedQueue<>(10, 100);

    @Test
    public void offer() {
        // 1. Test normal offer execution
        queue.offer("abc", 5);
        Assert.assertEquals("getCount should be 1", 1, queue.getCount());
        // 2. Test situations that exceed the MaxSize
        for (int i = 0; i < 9; i++) {
            queue.offer("abc", i);
        }
        Assert.assertFalse(
            "The last piece of data should not be inserted.", queue.offer("last", 1));
        // 3. Test situations that exceed the MaxBytes
        queue.clear();
        queue.offer("abc", 5);
        Assert.assertFalse("Beyond maxBytes, data should not be inserted.", queue.offer("last", 100));
    }

    @Test
    public void drainTo() {
        //1. When the test team is listed as empty, it times out and returns 0.
        AgentBufferNextMessage<String> consumer = AgentBufferNextMessage.create(new NoOpEncoder<>(), 100, TimeUnit.SECONDS.toNanos(1));
        int result = queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1));
        Assert.assertEquals("The queue is empty and the result should be 0 normally.", 0, result);
        //2. Normal performance of the test
        for (int i = 0; i < 10; i++) {
            queue.offer("test" + i, 1);
        }
        result = queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1));
        Assert.assertEquals("The normal result should be 10..", 10, result);
        Assert.assertEquals("The queue should be empty now.", 0, queue.getCount());
        Assert.assertEquals("The number of bytes of the remaining data in the queue should be 0", 0, queue.getSizeInBytes());
        //3. add 100 object and then consumer object
        queue = new AgentByteBoundedQueue<>(10, 100);
        for (int i = 0; i < 100; i++) {
            queue.offer("test" + i, 1);
        }
        result = queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1));
        Assert.assertEquals("Teh normal result should be 10..", 10, result);
        Assert.assertEquals("The queue should be empty now.", 0, queue.getCount());
        Assert.assertEquals("The number of bytes of the remaining data in the queue should be 0", 0, queue.getSizeInBytes());
        Assert.assertEquals("The amount of data lost should be 90.", 90, queue.getLoseCount());
    }

    @Test
    public void multiThreadProductConsumerTest() throws InterruptedException {
        AgentByteBoundedQueue<String> queue = new AgentByteBoundedQueue<>(100, 100);
        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger loseCount = new AtomicInteger();
        AtomicInteger consumerCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    if (queue.offer(UUID.randomUUID().toString(), 1)) {
                        successCount.incrementAndGet();
                    } else {
                        loseCount.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        new Thread(() -> {
            AgentBufferNextMessage<String> consumer = AgentBufferNextMessage.create(new NoOpEncoder<>(), 1000000, TimeUnit.SECONDS.toNanos(1));
            consumerCount.addAndGet(queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1)));
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            consumerCount.addAndGet(queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1)));
            synchronousQueue.offer(new Object());
        }).start();
        synchronousQueue.take();
        Assert.assertEquals("The data of the queue should be 0", 0, queue.getCount());
        Assert.assertEquals("The number of bytes of data stored in the queue should be 0", 0, queue.getSizeInBytes());
        Assert.assertTrue("There is data loss in the queue. LoseCount should be greater than 0.", queue.getLoseCount() > 0L);
        Assert.assertEquals("The number of losses within the queue should be consistent with the value of the external record", loseCount.intValue(), queue.getLoseCount());
        Assert.assertEquals("The amount of consumption should be consistent with the number of successes.", successCount.intValue(), consumerCount.intValue());
    }


    @Test
    public void testDatNotLostOnDrainToFail() throws Exception {
        AgentByteBoundedQueue<String> queue = new AgentByteBoundedQueue<>(1000, 1000);
        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        SynchronousQueue<Object> synchronousQueue = new SynchronousQueue<>();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger loseCount = new AtomicInteger();
        AtomicInteger consumerCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    if (queue.offer(UUID.randomUUID().toString(), 1)) {
                        successCount.incrementAndGet();
                    } else {
                        loseCount.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        new Thread(() -> {
            AgentBufferNextMessage<String> consumer = AgentBufferNextMessage.create(new NoOpEncoder<>(), 500, TimeUnit.SECONDS.toNanos(1));
            consumerCount.addAndGet(queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1)));
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            consumerCount.addAndGet(queue.drainTo(consumer, TimeUnit.SECONDS.toNanos(1)));
            synchronousQueue.offer(new Object());
        }).start();
        synchronousQueue.take();
        Assert.assertEquals("The data of the queue should be 500", 500, queue.getCount());
        Assert.assertEquals("The number of bytes of data stored in the queue should be 0", 500, queue.getSizeInBytes());
        Assert.assertTrue("There is data loss in the queue. LoseCount should be greater than 0.", queue.getLoseCount() == 0L);
        Assert.assertEquals("The amount of consumption should be consistent with the number of successes.", 500, consumerCount.intValue());
    }


    @Test
    public void clear() {
        for (int i = 0; i < 10; i++) {
            queue.offer("test" + i, 1);
        }
        Assert.assertEquals("The queue size should be 10", 10, queue.getCount());
        queue.clear();
        Assert.assertEquals("The queue size should be 0", 0, queue.getCount());
        Assert.assertEquals("The number of bytes of data in the queue should be 0", 0, queue.getSizeInBytes());
    }

}
