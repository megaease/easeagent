package com.hexdecteam.easeagent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Subscription {
    /** @param host should has one method annotated with {@link Consumer} at least. */
    void register(Object host);

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface Consume {}
}
