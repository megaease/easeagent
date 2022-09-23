package com.megaease.easeagent.report.async.zipkin;

import com.megaease.easeagent.report.plugin.NoOpEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
