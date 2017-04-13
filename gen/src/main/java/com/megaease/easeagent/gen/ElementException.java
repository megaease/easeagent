package com.megaease.easeagent.gen;

import javax.lang.model.element.Element;

class ElementException extends RuntimeException {
    final Element element;

    public ElementException(Element element, String message) {
        super(element + " " + message);
        this.element = element;
    }
}
