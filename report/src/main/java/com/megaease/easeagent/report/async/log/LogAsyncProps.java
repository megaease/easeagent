/*
 * Copyright (c) 2022, MegaEase
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
package com.megaease.easeagent.report.async.log;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.report.async.AsyncProps;

import static com.megaease.easeagent.config.ConfigUtils.bindProp;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class LogAsyncProps implements AsyncProps {
    private volatile int reportThread;
    private volatile int queuedMaxLogs;
    private volatile int queuedMaxSize;
    private volatile int messageTimeout;
    private volatile int messageMaxBytes;

    public LogAsyncProps(Config config) {
        int onePercentageMemory = AsyncProps.onePercentOfMemory();
        bindProp(LOG_ASYNC_REPORT_THREAD, config, Config::getInt, v -> this.reportThread = v, 1);
        bindProp(LOG_ASYNC_QUEUED_MAX_SIZE, config, Config::getInt, v -> this.queuedMaxSize = v, onePercentageMemory);
        bindProp(LOG_ASYNC_QUEUED_MAX_LOGS, config, Config::getInt, v -> this.queuedMaxLogs = v, 500);
        bindProp(LOG_ASYNC_MESSAGE_MAX_BYTES, config, Config::getInt, v -> this.messageMaxBytes = v, 999900);
        bindProp(LOG_ASYNC_MESSAGE_TIMEOUT, config, Config::getInt, v -> this.messageTimeout = v, 1000);
    }

    @Override
    public int getReportThread() {
        return this.reportThread;
    }

    @Override
    public int getQueuedMaxItems() {
        return this.queuedMaxLogs;
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
