package com.megaease.easeagent.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConfigConstTest {
    @Test
    public void testExtractHeaderName() {
        String prefix = "globalCanaryHeaders.serviceHeaders";
        assertEquals(prefix, ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS);
        assertEquals("hello", ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-aaa.0.hello"));
        assertEquals("world", ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-aaa.1.world"));
        assertNull(ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + ".test-bbb"));
        assertNull(ConfigConst.GlobalCanaryLabels.extractHeaderName(prefix + "test-bbb"));
    }
}