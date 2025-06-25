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

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;

import java.util.Map;

import static com.megaease.easeagent.config.ConfigUtils.bindProp;
import static com.megaease.easeagent.config.ConfigUtils.isChanged;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;
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

    boolean updateConfig(Map<String, String> changed);

    static OutputProperties newDefault(Config configs) {
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


        public Default(Config configs) {
            extractProp(configs);
        }

        @Override
        public boolean updateConfig(Map<String, String> changed) {
            Configs configs = new Configs(changed);
            int changeItems = 0;
            changeItems += isChanged(OUTPUT_SERVERS, changed, this.servers);
            changeItems += isChanged(OUTPUT_TIMEOUT, changed, this.timeout);
            changeItems += isChanged(OUTPUT_ENABLED, changed, String.valueOf(this.enabled));
            changeItems += isChanged(OUTPUT_SECURITY_PROTOCOL, changed, this.protocol);
            changeItems += isChanged(OUTPUT_SSL_KEYSTORE_TYPE, changed, this.sslKeyStoreType);
            changeItems += isChanged(OUTPUT_KEY, changed, this.sslKey);
            changeItems += isChanged(OUTPUT_CERT, changed, this.certificate);
            changeItems += isChanged(OUTPUT_TRUST_CERT, changed, this.trustCertificate);
            changeItems += isChanged(OUTPUT_TRUST_CERT_TYPE, changed, this.trustCertificateType);
            changeItems += isChanged(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM, changed, this.endpointAlgorithm);

            // if there are v2 configuration items, override with v2 config.
            changeItems += isChanged(BOOTSTRAP_SERVERS, changed, this.servers);
            changeItems += isChanged(OUTPUT_SERVERS_TIMEOUT, changed, this.timeout);
            changeItems += isChanged(OUTPUT_SERVERS_ENABLE, changed, String.valueOf(this.enabled));
            changeItems += isChanged(OUTPUT_SECURITY_PROTOCOL_V2, changed, this.protocol);
            changeItems += isChanged(OUTPUT_SSL_KEYSTORE_TYPE_V2, changed, this.sslKeyStoreType);
            changeItems += isChanged(OUTPUT_KEY_V2, changed, this.sslKey);
            changeItems += isChanged(OUTPUT_CERT_V2, changed, this.certificate);
            changeItems += isChanged(OUTPUT_TRUST_CERT_V2, changed, this.trustCertificate);
            changeItems += isChanged(OUTPUT_TRUST_CERT_TYPE_V2, changed, this.trustCertificateType);
            changeItems += isChanged(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM_V2, changed, this.endpointAlgorithm);
            if (changeItems == 0) {
                return false;
            }
            extractProp(configs);
            return true;
        }

        private void extractProp(Config configs) {
            bindProp(OUTPUT_SERVERS, configs, Config::getString, v -> this.servers = v);
            bindProp(OUTPUT_TIMEOUT, configs, Config::getString, v -> this.timeout = v);
            bindProp(OUTPUT_ENABLED, configs, Config::getBoolean, v -> this.enabled = v);
            bindProp(OUTPUT_SECURITY_PROTOCOL, configs, Config::getString, v -> this.protocol = v);
            bindProp(OUTPUT_SSL_KEYSTORE_TYPE, configs, Config::getString, v -> this.sslKeyStoreType = v);
            bindProp(OUTPUT_KEY, configs, Config::getString, v -> this.sslKey = v);
            bindProp(OUTPUT_CERT, configs, Config::getString, v -> this.certificate = v);
            bindProp(OUTPUT_TRUST_CERT, configs, Config::getString, v -> this.trustCertificate = v);
            bindProp(OUTPUT_TRUST_CERT_TYPE, configs, Config::getString, v -> this.trustCertificateType = v);
            bindProp(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM, configs, Config::getString, v -> this.endpointAlgorithm = v);

            // if there are v2 configuration items, override with v2 config.
            bindProp(BOOTSTRAP_SERVERS, configs, Config::getString, v -> this.servers = v);
            bindProp(OUTPUT_SERVERS_TIMEOUT, configs, Config::getString, v -> this.timeout = v);
            bindProp(OUTPUT_SERVERS_ENABLE, configs, Config::getBoolean, v -> this.enabled = v);
            bindProp(OUTPUT_SECURITY_PROTOCOL_V2, configs, Config::getString, v -> this.protocol = v);
            bindProp(OUTPUT_SSL_KEYSTORE_TYPE_V2, configs, Config::getString, v -> this.sslKeyStoreType = v);
            bindProp(OUTPUT_KEY_V2, configs, Config::getString, v -> this.sslKey = v);
            bindProp(OUTPUT_CERT_V2, configs, Config::getString, v -> this.certificate = v);
            bindProp(OUTPUT_TRUST_CERT_V2, configs, Config::getString, v -> this.trustCertificate = v);
            bindProp(OUTPUT_TRUST_CERT_TYPE_V2, configs, Config::getString, v -> this.trustCertificateType = v);
            bindProp(OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM_V2, configs, Config::getString, v -> this.endpointAlgorithm = v);
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
