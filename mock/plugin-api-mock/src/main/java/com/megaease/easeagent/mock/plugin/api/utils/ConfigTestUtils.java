/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.mock.plugin.api.utils;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;

import java.io.Closeable;
import java.util.Collections;

public class ConfigTestUtils {


    /**
     * change boolean ${@code property} to ${@code value } to ${@code iPluginConfig}
     *
     * @param iPluginConfig ${@link IPluginConfig} the config change for
     * @param property      String the config property change for
     * @param value         boolean the value change for
     * @return Reset It must be call ${@link Reset#clone()} after your business.
     */
    public static Reset changeBoolean(IPluginConfig iPluginConfig, String property, boolean value) {
        String name = ConfigUtils.buildPluginProperty(iPluginConfig.domain(), iPluginConfig.namespace(), iPluginConfig.id(), property);
        Boolean oldValue = iPluginConfig.getBoolean(property);
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(name, String.valueOf(value)));
        return new Reset(name, String.valueOf(oldValue));
    }


    /**
     * change String ${@code property} to ${@code value } to ${@code iPluginConfig}
     *
     * @param iPluginConfig ${@link IPluginConfig} the config change for
     * @param property      String the config property change for
     * @param value         String the value change for
     * @return Reset It must be call ${@link Reset#clone()} after your business.
     */

    public static Reset changeString(IPluginConfig iPluginConfig, String property, String value) {
        String name = ConfigUtils.buildPluginProperty(iPluginConfig.domain(), iPluginConfig.namespace(), iPluginConfig.id(), property);
        String oldValue = iPluginConfig.getString(property);
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(name, value));
        return new Reset(name, oldValue);
    }

    /**
     * change String ${@code name} to ${@code value } to global Configs
     *
     * @param name  String the config property change for
     * @param value String the value change for
     * @return Reset It must be call ${@link Reset#clone()} after your business.
     */

    public static Reset changeConfig(String name, String value) {
        String oldValue = MockConfig.getCONFIGS().getString(name);
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(name, value));
        return new Reset(name, oldValue);
    }

    /**
     * A Reset for Configs
     * It must be call ${@link Reset#close()} after your business.
     *
     * <pre>${@code
     *  try (ConfigTestUtils.Reset ignored = ConfigTestUtils.changeConfig(key, value)) {
     *      //do test
     *  }
     * }</pre>
     */
    public static class Reset implements Closeable {
        private final String name;
        private final String value;

        public Reset(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void close() {
            MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(name, value));
        }
    }
}
