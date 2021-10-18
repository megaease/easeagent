package com.megaease.easeagent.log4j2;

import com.megaease.easeagent.log4j2.impl.AgentLogger;
import com.megaease.easeagent.log4j2.impl.AgentLoggerFactory;
import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

public class AgentLoggerFactoryTest {
    static {
        AllUrlsSupplier.ENABLED = true;
//        String jar = "jar:file:/Users/beyond/IdeaProjects/easeagent_dir/spring-petclinic-microservices/generated/agents/vets-service/easeagent-dep.jar!/log4j2/log4j2-1.0.0.jar!/";
//        String jar = "/Users/beyond/IdeaProjects/easeagent_dir/spring-petclinic-microservices/generated/agents/vets-service/easeagent-dep.jar!/log4j2/log4j2-1.0.0.jar";
//        System.setProperty("EASEAGENT-SLF4J2-LIB-JAR-PATHS", jar);
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
