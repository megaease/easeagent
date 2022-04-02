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
package com.megaease.easeagent.report.encoder.log.pattern;

import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;

public class LogDataThreadNamePatternConverter extends LogDataPatternConverter {
    public static final LogDataThreadNamePatternConverter INSTANCE = new LogDataThreadNamePatternConverter();
    /**
     * Create a new pattern converter.
     */
    protected LogDataThreadNamePatternConverter() {
        super("Thread", "thread");
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        toAppendTo.append(event.getThreadName());
    }
}
