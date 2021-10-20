package com.megaease.easeagent.log4j2.impl;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.log4j2.MDC;
import com.megaease.easeagent.log4j2.api.AgentLogger;
import com.megaease.easeagent.log4j2.api.AgentLoggerFactory;
import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import com.megaease.easeagent.log4j2.supplier.URLClassLoaderSupplier;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Function;

import static org.junit.Assert.*;

public class AgentLoggerFactoryTest {
    static {
        AllUrlsSupplier.ENABLED = true;
    }

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

    @Test
    public void getLogger() {
        Logger logger = LoggerFactory.getLogger(AgentLoggerFactoryTest.class.getName());
        logger.info("aaaa");
        MDC.put("testMdc", "testMdc_value");
        logger.info("bbbb");
    }


    @Test
    public void newFactory() {
        AgentLoggerFactory<TestAgentLogger> factory = LoggerFactory.newFactory(TestAgentLogger.LOGGER_SUPPLIER, TestAgentLogger.class);
        TestAgentLogger logger = factory.getLogger(AgentLoggerFactoryTest.class.getName());
        logger.info("aaaa");
        factory.mdc().put("newFactory", "newFactory");
        assertNotNull(MDC.get("newFactory"));

    }

    static class TestAgentLogger extends AgentLogger {
        public static final Function<java.util.logging.Logger, TestAgentLogger> LOGGER_SUPPLIER = logger -> new TestAgentLogger(logger);

        public TestAgentLogger(java.util.logging.Logger logger) {
            super(logger);
        }
    }
}
