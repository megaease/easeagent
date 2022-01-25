/*
 * Copyright (c) 2021, MegaEase
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
 *
 */
package com.megaease.easeagent.report.async;

import com.megaease.easeagent.plugin.api.config.Config;

import static com.megaease.easeagent.config.ConfigUtils.bindProp;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public interface TraceAsyncProps {
    int getReportThread();

    int getQueuedMaxSpans();

    long getMessageTimeout();

    int getQueuedMaxSize();

    int getMessageMaxBytes();

    static TraceAsyncProps newDefault(Config cfg) {
        return new Default(cfg);
    }

    class Default implements TraceAsyncProps {
        private volatile int reportThread;
        private volatile int queuedMaxSpans;
        private volatile int queuedMaxSize;
        private volatile int messageTimeout;
        private volatile int messageMaxBytes;

        Default(Config config) {
            bindProp(TRACE_ASYNC_REPORT_THREAD_V2, config, Config::getInt, v -> this.reportThread = v, 1);
            bindProp(TRACE_ASYNC_QUEUED_MAX_SIZE_V2, config, Config::getInt, v -> this.queuedMaxSize = v, 1000000);
            bindProp(TRACE_ASYNC_QUEUED_MAX_SPANS_V2, config, Config::getInt, v -> this.queuedMaxSpans = v, 1000);
            bindProp(TRACE_ASYNC_MESSAGE_MAX_BYTES_V2, config, Config::getInt, v -> this.messageMaxBytes = v, 999900);
            bindProp(TRACE_ASYNC_MESSAGE_TIMEOUT_V2, config, Config::getInt, v -> this.messageTimeout = v, 1000);
        }

        @Override
        public int getReportThread() {
            return this.reportThread;
        }

        @Override
        public int getQueuedMaxSpans() {
            return this.queuedMaxSpans;
        }

        @Override
        public long getMessageTimeout() {
            return this.messageTimeout;
        }

        @Override
        public int getQueuedMaxSize() {
            return this.queuedMaxSize;
        }

        @Override
        public int getMessageMaxBytes() {
            return this.messageMaxBytes;
        }
    }
}
