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

import java.util.ArrayList;
import java.util.List;

public abstract class LogDataPatternConverter extends LogEventPatternConverter {
    protected LogDataPatternConverter() {
        super("", "");
    }
    /**
     * Create a new pattern converter.
     *
     * @param name  name for pattern converter.
     * @param style CSS style for formatted output.
     */
    protected LogDataPatternConverter(String name, String style) {
        super(name, style);
    }

    /**
     * Formats an event into a string buffer.
     *
     * @param event      event to format, may not be null.
     * @param toAppendTo string buffer to which the formatted event will be appended.  May not be null.
     */
    public abstract void format(final AgentLogData event, final StringBuilder toAppendTo);

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final Object obj, final StringBuilder output) {
        if (obj instanceof AgentLogData) {
            format((AgentLogData) obj, output);
        } else {
            super.format(obj, output);
        }
    }

    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        // ignored
    }

    protected String[] getOptions(String pattern, int start) {
        ArrayList<String> options = new ArrayList<>();
        extractOptions(pattern, start, options);
        return options.toArray(new String[0]);
    }

    /**
     * Extract options.
     * borrow from log4j:PatternParser
     *
     * @param pattern
     *            conversion pattern.
     * @param start
     *            start of options.
     * @param options
     *            array to receive extracted options
     * @return position in pattern after options.
     */
    @SuppressWarnings("UnusedReturnValue")
    private int extractOptions(final String pattern, final int start, final List<String> options) {
        int i = pattern.indexOf('{', start);
        if (i < 0) {
            return start;
        }
        while (i < pattern.length() && pattern.charAt(i) == '{') {
            i++; // skip opening "{"
            final int begin = i; // position of first real char
            int depth = 1; // already inside one level
            while (depth > 0 && i < pattern.length()) {
                final char c = pattern.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    // TODO(?) maybe escaping of { and } with \ or %
                }
                i++;
            } // while

            if (depth > 0) { // option not closed, continue with pattern after closing bracket
                i = pattern.lastIndexOf('}');
                if (i == -1 || i < start) {
                    // if no closing bracket could be found or there is no closing bracket behind the starting
                    // character of our parsing process continue parsing after the first opening bracket
                    return begin;
                }
                return i + 1;
            }

            options.add(pattern.substring(begin, i - 1));
        } // while

        return i;
    }
}
