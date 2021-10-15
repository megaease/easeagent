package com.megaease.easeagent.log4j2;

import org.junit.Test;

import static org.junit.Assert.*;

public class MDCTest {

    @Test
    public void put() {
        MDC.put("testA", "testB");
        assertNull(org.slf4j.MDC.get("testA"));
        assertNotNull(MDC.get("testA"));
    }

    @Test
    public void remove() {
        MDC.put("testA", "testB");
        assertNotNull(MDC.get("testA"));
        MDC.remove("testA");
        assertNull(MDC.get("testA"));
    }

    @Test
    public void get() {
        MDC.put("testA", "testB");
        assertNull(org.slf4j.MDC.get("testA"));
        assertNotNull(MDC.get("testA"));
        org.slf4j.MDC.put("testB", "testB");
        assertNotNull(org.slf4j.MDC.get("testB"));
        assertNull(MDC.get("testB"));
    }
}
