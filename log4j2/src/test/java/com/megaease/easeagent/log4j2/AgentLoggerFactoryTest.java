package com.megaease.easeagent.log4j2;

import com.megaease.easeagent.log4j2.impl.AgentLogger;
import com.megaease.easeagent.log4j2.impl.AgentLoggerFactory;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

public class AgentLoggerFactoryTest {


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
