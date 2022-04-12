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
import com.megaease.easeagent.plugin.api.otlp.common.SemanticKey;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Arrays;

/**
 * port from log4j2.MdcPatternConverter
 */
public class LogDataMdcPatternConverter extends LogDataPatternConverter {
    /**
     * Name of property to output.
     */
    private final String key;
    private final String[] keys;
    private final boolean full;

    // reference to log4j2's MdcPatternConverter
    public LogDataMdcPatternConverter(String[] options) {
        super(options != null && options.length > 0 ? "MDC{" + options[0] + '}' : "MDC", "mdc");
        if (options != null && options.length > 0) {
            full = false;
            if (options[0].indexOf(',') > -1) {
                String oKey;
                String[] oKeys = options[0].split(",");
                int idx = 0;
                for (int i = 0; i < oKeys.length; i++) {
                    oKey = oKeys[i].trim();
                    if (oKey.length() <= 0) {
                        continue;
                    }
                    oKeys[idx++] = oKey;
                }
                if (idx == 0) {
                    keys = null;
                    key = options[0];
                } else {
                    keys = Arrays.copyOf(oKeys, idx);
                    key = null;
                }
            } else {
                keys = null;
                key = options[0];
            }
        } else {
            full = true;
            key = null;
            keys = null;
        }
    }

    private static final TriConsumer<AttributeKey<?>, Object, StringBuilder> WRITE_KEY_VALUES_INTO = (k, value, sb) -> {
        sb.append(k.getKey()).append('=');
        StringBuilders.appendValue(sb, value);
        sb.append(", ");
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(AgentLogData event, StringBuilder toAppendTo) {
        final Attributes contextData = event.getAttributes();
        // if there is no additional options, we output every single
        // Key/Value pair for the MDC in a similar format to Hashtable.toString()
        if (full) {
            if (contextData == null || contextData.isEmpty()) {
                toAppendTo.append("{}");
                return;
            }
            appendFully(contextData, toAppendTo);
        } else if (keys != null) {
            if (contextData == null || contextData.isEmpty()) {
                toAppendTo.append("{}");
                return;
            }
            appendSelectedKeys(keys, contextData, toAppendTo);
        } else if (contextData != null){
            // otherwise they just want a single key output
            final Object value = contextData.get(SemanticKey.stringKey(key));
            if (value != null) {
                StringBuilders.appendValue(toAppendTo, value);
            }
        }
    }

    private static void appendFully(final Attributes contextData, final StringBuilder toAppendTo) {
        toAppendTo.append("{");
        final int start = toAppendTo.length();
        contextData.forEach((k, v) -> WRITE_KEY_VALUES_INTO.accept(k, v, toAppendTo));
        final int end = toAppendTo.length();
        if (end > start) {
            toAppendTo.setCharAt(end - 2, '}');
            toAppendTo.deleteCharAt(end - 1);
        } else {
            toAppendTo.append('}');
        }
    }

    private static void appendSelectedKeys(final String[] keys, final Attributes contextData, final StringBuilder sb) {
        // Print all the keys in the array that have a value.
        final int start = sb.length();
        sb.append('{');
        for (final String theKey : keys) {
            final Object value = contextData.get(SemanticKey.stringKey(theKey));
            if (value != null) {
                if (sb.length() - start > 1) {
                    sb.append(", ");
                }
                sb.append(theKey).append('=');
                StringBuilders.appendValue(sb, value);
            }
        }
        sb.append('}');
    }
}
