package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TransparentTransmissionTest {

    @Test
    public void init() {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put(TransparentTransmission.EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS, "aaa,bbb,ccc");
        Configs configs = new Configs(source);
        TransparentTransmission.init(configs);
        String[] fields = TransparentTransmission.getFields();
        assertFalse(TransparentTransmission.isEmpty(fields));
    }

    @Test
    public void isEmpty() {
        assertTrue(TransparentTransmission.isEmpty(null));
        assertTrue(TransparentTransmission.isEmpty(new String[0]));
        assertFalse(TransparentTransmission.isEmpty(new String[1]));
    }

    @Test
    public void getFields() {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put(TransparentTransmission.EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS, "aaa,bbb,ccc");
        Configs configs = new Configs(source);
        TransparentTransmission.init(configs);
        String[] fields = TransparentTransmission.getFields();
        assertFalse(TransparentTransmission.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("bbb", fields[1]);
        assertEquals("ccc", fields[2]);

        configs.updateConfigs(Collections.singletonMap(TransparentTransmission.EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS, "aaa,ccc"));
        fields = TransparentTransmission.getFields();
        assertFalse(TransparentTransmission.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("ccc", fields[1]);

        configs.updateConfigs(Collections.singletonMap(TransparentTransmission.EASEAGENT_TRANSPARENT_TRANSMISSION_FIELDS, "aaa,ddd,,ccc"));
        fields = TransparentTransmission.getFields();
        assertFalse(TransparentTransmission.isEmpty(fields));
        assertEquals("aaa", fields[0]);
        assertEquals("ddd", fields[1]);
        assertEquals("ccc", fields[2]);
    }
}
