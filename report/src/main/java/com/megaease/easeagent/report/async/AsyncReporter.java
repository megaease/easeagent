/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.report.async;

import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.report.sender.SenderWithEncoder;

import java.util.List;
import java.util.concurrent.ThreadFactory;

public interface AsyncReporter<S> extends ConfigChangeListener {
    void setFlushThreads(List<Thread> flushThreads);

    void setSender(SenderWithEncoder sender);

    SenderWithEncoder getSender();

    void setPending(int queuedMaxSpans, int queuedMaxBytes);

    void setMessageTimeoutNanos(long messageTimeoutNanos);

    /**
     * Returns true if the was encoded and accepted onto the queue.
     */
    void report(S next);

    void flush();

    boolean check();

    void close();

    void setThreadFactory(ThreadFactory threadFactory);

    void startFlushThread();

    void closeFlushThread();
}
