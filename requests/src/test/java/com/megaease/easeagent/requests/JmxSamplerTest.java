package com.megaease.easeagent.requests;

import brave.sampler.Sampler;
import org.junit.Test;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JmxSamplerTest {

    @Test
    public void should_create_mbean() throws Exception {
        final Sampler sampler = JmxSampler.create();

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final MBeanInfo info = server.getMBeanInfo(JmxSampler.Lazy.NAME);
        final MBeanOperationInfo[] operations = info.getOperations();
        final String operationName = "enable";

        assertThat(operations.length, is(1));
        assertThat(operations[0].getName(), is(operationName));
        assertThat(operations[0].getSignature()[0].getType(), is("int"));

        assertFalse(sampler.isSampled(0));

        final Object result = server.invoke(JmxSampler.Lazy.NAME, operationName, new Object[]{1}, new String[]{"int"});

        assertTrue((Boolean) result);

        assertTrue(sampler.isSampled(0));

        Thread.sleep(1100L);

        assertFalse(sampler.isSampled(0));
    }
}