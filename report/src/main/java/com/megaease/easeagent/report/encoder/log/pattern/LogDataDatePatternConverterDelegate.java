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
import org.apache.logging.log4j.core.pattern.DatePatternConverter;

import java.util.concurrent.TimeUnit;

public class LogDataDatePatternConverterDelegate extends LogDataPatternConverter {
    DatePatternConverter converter;

    /**
     * Create a new pattern converter.
     *
     * @param options  options.
     */
    protected LogDataDatePatternConverterDelegate(String[] options) {
        super("Date", "date");
        this.converter = DatePatternConverter.newInstance(options);
    }

    public LogDataDatePatternConverterDelegate(DatePatternConverter dateConverter) {
        super("Date", "date");
        this.converter = dateConverter;
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        this.converter.format(event.getEpochMillis(), toAppendTo);
    }
}
