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
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.util.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ported from log4j2.ThrowablePatternConverter
 */
public class LogDataThrowablePatternConverter extends LogDataPatternConverter {
    /**
     * Lists {@link LogDataPatternFormatter}s for the suffix attribute.
     */
    protected final List<LogDataPatternFormatter> formatters;
    private String rawOption;
    private final boolean subShortOption;
    private final boolean nonStandardLineSeparator;

    /**
     * Options.
     */
    protected final ThrowableFormatOptions options;

    public LogDataThrowablePatternConverter(String[] options) {
        super("Throwable", "throwable");
        this.options = ThrowableFormatOptions.newInstance(options);
        if (options != null && options.length > 0) {
            rawOption = options[0];
        }
        if (this.options.getSuffix() != null) {
            final PatternParser parser = PatternLayout.createPatternParser(null);
            final String suffixPattern = this.options.getSuffix();

            final List<LogDataPatternFormatter> parsedSuffixFormatters = LogDataPatternFormatter.transform(suffixPattern, parser);

            // filter out nested formatters that will handle throwable
            boolean hasThrowableSuffixFormatter = false;
            for (final LogDataPatternFormatter suffixFormatter : parsedSuffixFormatters) {
                if (suffixFormatter.handlesThrowable()) {
                    hasThrowableSuffixFormatter = true;
                }
            }
            if (!hasThrowableSuffixFormatter) {
                this.formatters = parsedSuffixFormatters;
            } else {
                final List<LogDataPatternFormatter> suffixFormatters = new ArrayList<>();
                for (final LogDataPatternFormatter suffixFormatter : parsedSuffixFormatters) {
                    if (!suffixFormatter.handlesThrowable()) {
                        suffixFormatters.add(suffixFormatter);
                    }
                }
                this.formatters = suffixFormatters;
            }
        } else {
            this.formatters = Collections.emptyList();
        }
        subShortOption = ThrowableFormatOptions.MESSAGE.equalsIgnoreCase(rawOption) ||
            ThrowableFormatOptions.LOCALIZED_MESSAGE.equalsIgnoreCase(rawOption) ||
            ThrowableFormatOptions.FILE_NAME.equalsIgnoreCase(rawOption) ||
            ThrowableFormatOptions.LINE_NUMBER.equalsIgnoreCase(rawOption) ||
            ThrowableFormatOptions.METHOD_NAME.equalsIgnoreCase(rawOption) ||
            ThrowableFormatOptions.CLASS_NAME.equalsIgnoreCase(rawOption);
        nonStandardLineSeparator = !Strings.LINE_SEPARATOR.equals(this.options.getSeparator());
    }

    @Override
    public void format(AgentLogData event, StringBuilder buffer) {
        final Throwable t = event.getThrowable();

        if (subShortOption) {
            formatSubShortOption(t, getSuffix(event), buffer);
        }
        else if (t != null && options.anyLines()) {
            formatOption(t, getSuffix(event), buffer);
        }
    }

    private void formatSubShortOption(final Throwable t, final String suffix, final StringBuilder buffer) {
        StackTraceElement[] trace;
        StackTraceElement throwingMethod = null;
        int len;

        if (t != null) {
            trace = t.getStackTrace();
            if (trace !=null && trace.length > 0) {
                throwingMethod = trace[0];
            }
        }

        if (t != null && throwingMethod != null) {
            String toAppend = Strings.EMPTY;

            if (ThrowableFormatOptions.CLASS_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getClassName();
            }
            else if (ThrowableFormatOptions.METHOD_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getMethodName();
            }
            else if (ThrowableFormatOptions.LINE_NUMBER.equalsIgnoreCase(rawOption)) {
                toAppend = String.valueOf(throwingMethod.getLineNumber());
            }
            else if (ThrowableFormatOptions.MESSAGE.equalsIgnoreCase(rawOption)) {
                toAppend = t.getMessage();
            }
            else if (ThrowableFormatOptions.LOCALIZED_MESSAGE.equalsIgnoreCase(rawOption)) {
                toAppend = t.getLocalizedMessage();
            }
            else if (ThrowableFormatOptions.FILE_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getFileName();
            }

            len = buffer.length();
            if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
                buffer.append(' ');
            }
            buffer.append(toAppend);

            if (Strings.isNotBlank(suffix)) {
                buffer.append(' ');
                buffer.append(suffix);
            }
        }
    }

    private void formatOption(final Throwable throwable, final String suffix, final StringBuilder buffer) {
        final int len = buffer.length();
        if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
            buffer.append(' ');
        }
        if (!options.allLines() || nonStandardLineSeparator || Strings.isNotBlank(suffix)) {
            final StringWriter w = new StringWriter();
            throwable.printStackTrace(new PrintWriter(w));

            final String[] array = w.toString().split(Strings.LINE_SEPARATOR);
            final int limit = options.minLines(array.length) - 1;
            final boolean suffixNotBlank = Strings.isNotBlank(suffix);
            for (int i = 0; i <= limit; ++i) {
                buffer.append(array[i]);
                if (suffixNotBlank) {
                    buffer.append(' ');
                    buffer.append(suffix);
                }
                if (i < limit) {
                    buffer.append(options.getSeparator());
                }
            }
        } else {
            throwable.printStackTrace(new PrintWriter(new StringBuilderWriter(buffer)));
        }
    }

    /**
     * This converter obviously handles throwables.
     *
     * @return true.
     */
    @Override
    public boolean handlesThrowable() {
        return true;
    }

    protected String getSuffix(final AgentLogData event) {
        if (formatters.isEmpty()) {
            return Strings.EMPTY;
        }

        //noinspection ForLoopReplaceableByForEach
        final StringBuilder toAppendTo = new StringBuilder();
        for (int i = 0, size = formatters.size(); i <  size; i++) {
            formatters.get(i).format(event, toAppendTo);
        }

        return toAppendTo.toString();
    }
}
