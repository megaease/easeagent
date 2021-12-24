/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.report.metric.log4j;

import com.google.common.collect.ImmutableList;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.report.OutputProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manage kafka's log4j appender according topics
 *
 * @author Kun Zhao
 * @version v1.0.2
 * @since V1.0.2
 */
public interface AppenderManager {
    Appender appender(String topic);

    void refresh();

    static AppenderManager create(OutputProperties outputProperties) {
        return new DefaultKafkaAppenderManager(outputProperties);
    }

    static AppenderManager create(Function<String, Appender> provider) {
        return new DefaultKafkaAppenderManager(null, provider);
    }

    final class DefaultKafkaAppenderManager implements AppenderManager {

        public static final Logger LOGGER = com.megaease.easeagent.log4j2.LoggerFactory.getLogger(DefaultKafkaAppenderManager.class);

        private Map<String, Appender> appenderMap = new ConcurrentHashMap<>();
        private final OutputProperties outputProperties;
        final LoggerContext context = LoggerFactory.getLoggerContext();
        Function<String, Appender> provider;

        private DefaultKafkaAppenderManager(OutputProperties outputProperties) {
            this.outputProperties = outputProperties;
            ClassLoader initClassLoader = Thread.currentThread().getContextClassLoader();
            LOGGER.info("bind classloader:{} to AppenderManager", initClassLoader);
            this.provider = (topic) -> {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(initClassLoader);
                try {
                    return this.newAppender(this.outputProperties, topic);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            };
        }

        private DefaultKafkaAppenderManager(OutputProperties outputProperties, Function<String, Appender> provider) {
            this.outputProperties = outputProperties;
            this.provider = provider;
        }

        @Override
        public Appender appender(String topic) {
            return appenderMap.computeIfAbsent(topic, this.provider);
        }

        private Appender newAppender(OutputProperties outputProperties, String topic) {
            if (StringUtils.isEmpty(outputProperties.getServers())) {
                return null;
            }
            try {
                String s = RandomStringUtils.randomAscii(8);

                List<Property> propertyList = new ArrayList<>();
                propertyList.add(Property.createProperty("bootstrap.servers", outputProperties.getServers()));
                propertyList.add(Property.createProperty("timeout.ms", outputProperties.getTimeout()));
                propertyList.add(Property.createProperty("acks", "0"));
                Property.createProperty(ProducerConfig.CLIENT_ID_CONFIG, "producer_" + topic + s);
                if (SecurityProtocol.SSL.name.equals(outputProperties.getSecurityProtocol())) {
                    propertyList.add(Property.createProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, outputProperties.getSecurityProtocol()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, outputProperties.getSSLKeyStoreType()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, outputProperties.getKeyStoreKey()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, outputProperties.getKeyStoreCertChain()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, outputProperties.getTrustCertificate()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, outputProperties.getTrustCertificateType()));
                    propertyList.add(Property.createProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, outputProperties.getEndpointAlgorithm()));
                }

                Property[] properties = new Property[propertyList.size()];
                propertyList.toArray(properties);
                Appender appender = KafkaAppender.newBuilder()
                    .setTopic(topic)
                    .setSyncSend(false)
                    .setName(topic + "_kafka_" + s)
                    .setPropertyArray(properties)
                    .setLayout(PatternLayout.newBuilder()
                        .withCharset(StandardCharsets.UTF_8)
                        .withConfiguration(context.getConfiguration())
                        .withPattern("%m%n").build())
                    .setConfiguration(context.getConfiguration())
                    .build();
                appender.start();
                return appender;
            } catch (Exception e) {
                LOGGER.warn("can't not create topic :" + topic + " kafka appender , error :", e.getMessage(), e);
            }
            return null;
        }

        @Override
        public void refresh() {
            Map<String, Appender> clearMap = this.appenderMap;
            this.appenderMap = new ConcurrentHashMap<>();
            ImmutableList<Appender> appenderList = ImmutableList.copyOf(clearMap.values());
            for (Appender a : appenderList) {
                try {
                    a.stop();
                } catch (Exception e) {
                    //
                }
            }
            clearMap.clear();
        }
    }
}
