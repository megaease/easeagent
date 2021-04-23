package com.megaease.easeagent.common;

public interface ContextCons {

    String CACHE_CMD = ContextCons.class.getName() + ".cache_cmd";
    String CACHE_URI = ContextCons.class.getName() + ".cache_uri";
    String MQ_URI = ContextCons.class.getName() + ".mq_uri";
    String ASYNC_FLAG = ContextCons.class.getName() + ".async";
    String SPAN = ContextCons.class.getName() + ".Span";
    String PROCESSED_BEFORE = ContextCons.class.getName() + ".Processed-Before";
    String PROCESSED_AFTER = ContextCons.class.getName() + ".Processed-After";
}
