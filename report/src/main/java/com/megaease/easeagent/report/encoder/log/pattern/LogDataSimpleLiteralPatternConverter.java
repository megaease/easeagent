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
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class LogDataSimpleLiteralPatternConverter extends LogDataPatternConverter {
    public static final LogDataPatternConverter UNKNOWN = new SimpleLiteralConverter("-Unknown Pattern-");

    LogEventPatternConverter converter;

    /**
     * Create a new pattern converter.
     */
    public LogDataSimpleLiteralPatternConverter(LogEventPatternConverter converter) {
        super("SimpleLiteral", "literal");
        this.converter = converter;
    }

    @Override
    public void format(final Object obj, final StringBuilder output) {
        this.converter.format(obj, output);
    }

    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        this.format((Object) event, toAppendTo);
    }

    public static class SimpleLiteralConverter extends LogDataPatternConverter {
        String literal;

        /**
         * Constructs an instance of LoggingEventPatternConverter.
         */
        protected SimpleLiteralConverter(String literal) {
            super("SimpleLiteral", "literal");
            this.literal = literal;
        }

        @Override
        public void format(AgentLogData event, StringBuilder toAppendTo) {
            toAppendTo.append(this.literal);
        }

        @Override
        public void format(LogEvent event, StringBuilder toAppendTo) {
            toAppendTo.append(this.literal);
        }
    }
}
