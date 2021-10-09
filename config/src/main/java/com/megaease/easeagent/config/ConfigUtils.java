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

package com.megaease.easeagent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.megaease.easeagent.config.ConfigConst.*;

public class ConfigUtils {
    public static String extractServiceName(Configs configs) {
        return configs.getString(ConfigConst.SERVICE_NAME);
    }

    public static <R> void bindProp(String name, Config configs, BiFunction<Config, String, R> func, Consumer<R> consumer, R def) {
        Runnable process = () -> {
            R result = func.apply(configs, name);
            result = firstNotNull(result, def);
            if (result != null) {
                consumer.accept(result);
            }
        };
        process.run();
        configs.addChangeListener(list -> {
            boolean hasChange = list.stream().map(ChangeItem::getFullName).anyMatch(fn -> fn.equals(name));
            if (hasChange) {
                process.run();
            }
        });
    }

    private static <R> R firstNotNull(R... ars) {
        for (R one : ars) {
            if (one != null) {
                return one;
            }
        }
        return null;
    }

    public static <R> void bindProp(String name, Config configs, BiFunction<Config, String, R> func, Consumer<R> consumer) {
        bindProp(name, configs, func, consumer, null);
    }

    public static Map<String, String> json2KVMap(String json) throws IOException {
        ObjectMapper JSON = new ObjectMapper();
        JsonNode node = JSON.readTree(json);
        List<Map.Entry<String, String>> list = extractKVs(null, node);
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static List<Map.Entry<String, String>> extractKVs(String prefix, JsonNode node) {
        List<Map.Entry<String, String>> rst = new LinkedList<>();
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String current = names.next();
                rst.addAll(extractKVs(join(prefix, current), node.path(current)));
            }
        } else if (node.isArray()) {
            int len = node.size();
            for (int i = 0; i < len; i++) {
                rst.addAll(extractKVs(join(prefix, i + ""), node.path(i)));
            }
        } else {
            rst.add(new AbstractMap.SimpleEntry<>(prefix, node.asText("")));
        }
        return rst;
    }

    private static String join(String prefix, String current) {
        return prefix == null ? current : ConfigConst.join(prefix, current);
    }

    public static boolean isSelf(String id) {
        return PLUGIN_SELF.equals(id);
    }

    public static boolean isPluginConfig(String key) {
        if (key.startsWith(PLUGIN_PREFIX)) {
            return true;
        }
        return false;
    }

    public static boolean isPluginConfig(String key, String domain, String namespace) {
        if (key.startsWith(ConfigConst.join(PLUGIN, domain, namespace))) {
            return true;
        }
        return false;
    }

    public static PluginProperty pluginProperty(String path) {
        String[] configs = path.split(DELIMITER);
        if (configs.length < 5) {
            throw new ValidateUtils.ValidException(String.format("Property[%s] must be format: %s", path, ConfigConst.join(PLUGIN, "<Domain>", "<Namespace>", "<Id>", "<Properties>")));
        }
        return new PluginProperty(configs[1], configs[2], configs[3], ConfigConst.join(Arrays.copyOfRange(configs, 4, configs.length)));
    }

}
