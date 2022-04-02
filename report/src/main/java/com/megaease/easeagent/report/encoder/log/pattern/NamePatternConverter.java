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
