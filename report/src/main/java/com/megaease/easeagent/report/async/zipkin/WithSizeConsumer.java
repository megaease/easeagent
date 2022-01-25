package com.megaease.easeagent.report.async.zipkin;

public interface WithSizeConsumer<S> {
    /** Returns true if the element could be added or false if it could not due to its size. */
    boolean offer(S next, int nextSizeInBytes);
}
