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
import org.apache.logging.log4j.core.pattern.*;

public class LogDataPatternFormatter extends LogDataPatternConverter {
    private final LogDataPatternConverter converter;
    private final FormattingInfo field;
    private final boolean skipFormattingInfo;

    public LogDataPatternFormatter(String pattern, int patternOffset,
                                   org.apache.logging.log4j.core.pattern.PatternFormatter formatter) {
        super("", "");
        this.field = formatter.getFormattingInfo();
        this.skipFormattingInfo = this.field == FormattingInfo.getDefault();
        this.converter = extractConvert(formatter.getConverter(), pattern, patternOffset);
    }

    public void format(final AgentLogData event, final StringBuilder buf) {
        if (skipFormattingInfo) {
            converter.format(event, buf);
        } else {
            formatWithInfo(event, buf);
        }
    }

    private void formatWithInfo(final AgentLogData event, final StringBuilder buf) {
        final int startField = buf.length();
        converter.format(event, buf);
        field.format(startField, buf);
    }

    private LogDataPatternConverter extractConvert(LogEventPatternConverter converter, String pattern, int patternOffset) {
        if (converter == null) {
            return NoOpPatternConverter.INSTANCE;
        }

        // xxx: can convert to name-INSTANCE map
        if (converter instanceof DatePatternConverter) {
            return new LogDataDatePatternConverterDelegate((DatePatternConverter)converter);
        } else if (converter instanceof LoggerPatternConverter) {
            return new LogDataLoggerPatternConverter(getOptions(pattern, patternOffset));
        } else if (converter instanceof LevelPatternConverter) {
            return new LogDataLevelPatternConverter();
        } else if (converter.getName().equals("SimpleLiteral")) {
            return new LogDataSimpleLiteralPatternConverter(converter);
        } else if (converter instanceof MessagePatternConverter) {
            return SimpleMessageConverter.INSTANCE;
        } else if (converter instanceof ThreadNamePatternConverter) {
            return LogDataThreadNamePatternConverter.INSTANCE;
        } else if (converter instanceof LineSeparatorPatternConverter) {
            return LogDataLineSeparatorPatternConverter.INSTANCE;
        } else {
            return LogDataSimpleLiteralPatternConverter.UNKNOWN;
        }
    }
}
