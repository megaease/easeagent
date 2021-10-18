package com.megaease.easeagent.log4j2.impl;

import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import com.megaease.easeagent.log4j2.supplier.URLClassLoaderSupplier;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class AgentLoggerFactoryTest {

    @Test
    public void builder() throws NoSuchMethodException, IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException, ClassNotFoundException {
        try {
            AgentLoggerFactory.builder(new URLClassLoaderSupplier(() -> new URL[0]), AgentLogger.LOGGER_SUPPLIER, AgentLogger.class).build();
            assertTrue("must be err", false);
        } catch (Exception e) {
            assertNotNull(e);
        }
        AgentLoggerFactory<?> factory = AgentLoggerFactory.builder(new URLClassLoaderSupplier(new AllUrlsSupplier()), AgentLogger.LOGGER_SUPPLIER, AgentLogger.class).build();

    }
}
