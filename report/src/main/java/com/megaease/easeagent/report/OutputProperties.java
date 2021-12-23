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

package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import org.apache.kafka.common.config.SslConfigs;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.*;

public interface OutputProperties {
    String getServers();

    String getTimeout();

    Boolean isEnabled();

    String getSecurityProtocol();

    String getSSLKeyStoreType();

    String getKeyStoreKey();

    String getKeyStoreCertChain();

    String getTrustCertificate();

    String getTrustCertificateType();

    String getEndpointAlgorithm();


    static OutputProperties newDefault(Configs configs) {
        return new Default(configs);
    }

    class Default implements OutputProperties {
        private volatile String endpointAlgorithm = "";
        private volatile String trustCertificateType = "";
        private volatile String trustCertificate = "";
        private volatile String servers = "";
        private volatile String timeout = "";
        private volatile boolean enabled;
        private volatile String protocol = "";
        private volatile String sslKeyStoreType = "";
        private volatile String sslKey = "";
        private volatile String certificate = "";


        public Default(Configs configs) {
            ConfigUtils.bindProp(OUTPUT_SERVERS, configs, Config::getString, v -> this.servers = v);
            ConfigUtils.bindProp(OUTPUT_TIMEOUT, configs, Config::getString, v -> this.timeout = v);
            ConfigUtils.bindProp(OUTPUT_ENABLED, configs, Config::getBoolean, v -> this.enabled = v);
            ConfigUtils.bindProp(OUTPUT_SECURITY_PROTOCOL, configs, Config::getString, v -> this.protocol = v);
            ConfigUtils.bindProp(OUTPUT_SSL_KEYSTORE_TYPE, configs, Config::getString, v -> this.sslKeyStoreType = v);
            ConfigUtils.bindProp(OUTPUT_KEY, configs, Config::getString, v -> this.sslKey = v);
            ConfigUtils.bindProp(OUTPUT_CERT, configs, Config::getString, v -> this.certificate = v);
            ConfigUtils.bindProp(OUTPUT_TRUST_CERT, configs, Config::getString, v -> this.trustCertificate = v);
            ConfigUtils.bindProp(OUTPUT_TRUST_CERT_TYPE, configs, Config::getString, v -> this.trustCertificateType = v);
            ConfigUtils.bindProp(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM, configs, Config::getString, v -> this.endpointAlgorithm = v);
        }

        @Override
        public String getServers() {
            return this.servers;
        }

        @Override
        public String getTimeout() {
            return this.timeout;
        }

        @Override
        public Boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public String getSecurityProtocol() {
            return this.protocol;
        }

        @Override
        public String getSSLKeyStoreType() {
            return this.sslKeyStoreType;
        }

        @Override
        public String getKeyStoreKey() {
            return this.sslKey;
        }

        @Override
        public String getKeyStoreCertChain() {
            return this.certificate;
        }

        @Override
        public String getTrustCertificate() {
            return this.trustCertificate;
        }

        @Override
        public String getTrustCertificateType() {
            return this.trustCertificateType;
        }

        @Override
        public String getEndpointAlgorithm() {
            return this.endpointAlgorithm;
        }
    }
}
