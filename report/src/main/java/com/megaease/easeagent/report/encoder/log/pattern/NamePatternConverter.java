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

import org.apache.logging.log4j.core.pattern.NameAbbreviator;

public abstract class NamePatternConverter extends LogDataPatternConverter {
    private final NameAbbreviator abbreviator;

    /**
     * Constructor.
     *
     * @param name    name of converter.
     * @param style   style name for associated output.
     * @param options options, may be null, first element will be interpreted as an abbreviation pattern.
     */
    protected NamePatternConverter(final String name, final String style, final String[] options) {
        super(name, style);

        if (options != null && options.length > 0) {
            abbreviator = NameAbbreviator.getAbbreviator(options[0]);
        } else {
            abbreviator = NameAbbreviator.getDefaultAbbreviator();
        }
    }

    /**
     * Abbreviate name in string buffer.
     *
     * @param original string containing name.
     * @param destination the StringBuilder to write to
     */
    protected final void abbreviate(final String original, final StringBuilder destination) {
        abbreviator.abbreviate(original, destination);
    }
}
