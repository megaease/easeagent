package com.megaease.easeagent.gen;

import com.google.common.base.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

interface Generated {
    String PREFIX = "Gen";

    Function<Element, ExecutableElement> CAST_TO_EXECUTABLE_ELEMENT = new Function<Element, ExecutableElement>() {
        @Override
        public ExecutableElement apply(Element input) {
            return (ExecutableElement) input;
        }
    };
}
