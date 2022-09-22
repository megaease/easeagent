package com.megaease.easeagent.report.async.zipkin;

import com.megaease.easeagent.report.plugin.NoOpEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class AgentByteBoundedQueueTest {

    private final AgentByteBoundedQueue<String> queue = new AgentByteBoundedQueue<>(10, 100);

    @Test
    public void offer() {
        // 1. 测试普通offer
        // 2. 测试超过maxSize
        // 3. 测试超过maxBytes
        queue.offer("abc", 5);
        Assert.assertEquals("getCount should be 1", 1, queue.getCount());
        for (int i = 0; i < 9; i++) {
            queue.offer("abc", i);
        }
        Assert.assertFalse(
            "The last piece of data should not be inserted.", queue.offer("last", 1));
        queue.clear();
        queue.offer("abc", 5);
        Assert.assertFalse("Beyond maxBytes, data should not be inserted.", queue.offer("last", 100));
    }

    @Test
    public void drainTo() {
        //1. 测试队列为空时，直接超时并返回0。
        //2. 测试正常情况
        AgentBufferNextMessage<String> message = AgentBufferNextMessage.create(new NoOpEncoder<>(), 100, TimeUnit.SECONDS.toNanos(1));
        int result = queue.drainTo(message, TimeUnit.SECONDS.toNanos(1));
        Assert.assertEquals("The queue is empty and the result should be 0 normally.", 0, result);
        for (int i = 0; i < 10; i++) {
            queue.offer("test" + i, 1);
        }
        result = queue.drainTo(message, TimeUnit.SECONDS.toNanos(1));
        Assert.assertEquals("The normal result should be 5..", 5, result);
        Assert.assertEquals("There should be half the elements in the queue.", 5, queue.getCount());
        Assert.assertEquals("The number of bytes of the remaining data in the queue should be 5", 5, queue.getSizeInBytes());
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
