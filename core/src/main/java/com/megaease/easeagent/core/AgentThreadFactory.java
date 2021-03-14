package com.megaease.easeagent.core;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadFactory;

public class AgentThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(@Nullable Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
