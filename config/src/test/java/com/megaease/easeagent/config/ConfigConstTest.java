package com.megaease.easeagent.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigConstTest {
    @Test
    public void testExtractHeaderName() {
        String prefix = "canary.filter.headers";
        assertEquals(prefix, ConfigConst.Canary.FILTER_HEADERS);
        assertEquals("test-aaa", ConfigConst.Canary.extractHeaderName(prefix + ".test-aaa.hello"));
        assertEquals("test-bbb", ConfigConst.Canary.extractHeaderName(prefix + ".test-bbb"));
        assertNull(ConfigConst.Canary.extractHeaderName(prefix + "test-bbb"));
    }
}