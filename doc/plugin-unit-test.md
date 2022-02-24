# Plugin Unit Test

This guide walks you through the process of unit testing a Plugin with Mock.

## Add Mock jar

For Maven builds, you can do that with the following:

```xml
<dependency>
    <groupId>com.megaease.easeagent</groupId>
    <artifactId>plugin-api-mock</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Create Test class

Now you can create a test class for a Interceptor, as the file (from `src/test/java/com/megaease/easeagent/plugin/interceptor/RunnableInterceptorTest.java`) shows:

```java
package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RunnableInterceptorTest {

    @Test
    public void before() throws InterruptedException {
        Context context = EaseAgent.getContext();
        final Span span = context.nextSpan();
        span.start();
        span.cacheScope();
        RunnableInterceptor runnableInterceptor = new RunnableInterceptor();
        AtomicInteger run = new AtomicInteger();
        Runnable runnable = () -> {
            Context runCont = EaseAgent.getContext();
            assertTrue(runCont.currentTracing().hasCurrentSpan());
            Span span1 = runCont.nextSpan();
            assertEquals(span.traceId(), span1.traceId());
            assertEquals(span.spanId(), span1.parentId());
            assertNotEquals(span.spanId(), span1.spanId());
            run.incrementAndGet();
        };
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker("")
            .type("")
            .method("")
            .args(new Object[]{runnable})
            .build();
        runnableInterceptor.before(methodInfo, context);
        Thread thread = new Thread((Runnable) methodInfo.getArgs()[0]);
        thread.start();
        thread.join();
        assertEquals(run.get(), 1);
        span.finish();

        ReportSpan span1 = MockEaseAgent.getLastSpan();
        assertEquals(span.traceIdString(), span1.traceId());
        assertEquals(span.parentIdString(), span1.parentId());
        assertEquals(span.spanIdString(), span1.id());
        System.out.println("run count: " + run.get());
    }
}
```
 
The class is flagged as a `@RunWith(EaseAgentJunit4ClassRunner.class)`, meaning it is ready for use by Mock EaseAgent to handle junit4 test. 
The method is flagged as a `@Test` is junit annotation. 

## Unit Test API

When the class is flagged with `@RunWith(EaseAgentJunit4ClassRunner.class)`, you can use the EaseAgent API in the method like Interceptor

Example:
```
Context context = EaseAgent.getContext();
        final Span span = context.nextSpan();
        span.start();
```
```
MethodInfo methodInfo = MethodInfo.builder()
    .invoker("")
    .type("")
    .method("")
    .args(new Object[]{runnable})
    .build();
```

You may have tested the Tracing or Metric generation process. At this time, you need an additional API to get the result, and then verify

### MockEaseAgent API
see:

* [MockEaseAgent](../mock/plugin-api-mock/src/main/java/com/megaease/easeagent/mock/plugin/api/MockEaseAgent.java)

* [LastJsonReporter](../mock/report-mock/src/main/java/com/megaease/easeagent/mock/report/impl/LastJsonReporter.java)

* [ReportSpan](../plugin-api/src/main/java/com/megaease/easeagent/plugin/report/tracing/ReportSpan.java)


```
// Verify that the Span has been started and finished and reports
Span span = EaseAgent.getContext().nextSpan();
span.start().finish();
ReportSpan reportSpan = MockEaseAgent.getLastSpan();
SpanTestUtils.sameId(span, MockEaseAgent.getLastSpan());

//verify metric count==1
LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(TagVerifier.build(tags, key)::verifyAnd);
Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
```

for demo see: 
* [MockEaseAgentTest](../mock/plugin-api-mock/src/test/java/com/megaease/easeagent/mock/plugin/api/demo/MockEaseAgentTest.java)
* [InterceptorTest](../mock/plugin-api-mock/src/test/java/com/megaease/easeagent/mock/plugin/api/demo/InterceptorTest.java)
