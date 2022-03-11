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
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.*;

public class ConfigUtils {
    private ConfigUtils() {}

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

    @SafeVarargs
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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
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

    public static boolean isGlobal(String namespace) {
        return PLUGIN_GLOBAL.equals(namespace);
    }

    public static boolean isPluginConfig(String key) {
        return key != null && key.startsWith(PLUGIN_PREFIX);
    }

    public static boolean isPluginConfig(String key, String domain, String namespace, String id) {
        return key != null && key.startsWith(ConfigConst.join(PLUGIN, domain, namespace, id));
    }

    public static PluginProperty pluginProperty(String path) {
        String[] configs = path.split("\\" + DELIMITER);
        if (configs.length < 5) {
            throw new ValidateUtils.ValidException(String.format("Property[%s] must be format: %s", path, ConfigConst.join(PLUGIN, "<Domain>", "<Namespace>", "<Id>", "<Properties>")));
        }

        for (int idOffsetEnd = 3; idOffsetEnd < configs.length - 1; idOffsetEnd++) {
            new PluginProperty(configs[1], configs[2],
                ConfigConst.join(Arrays.copyOfRange(configs, 3, idOffsetEnd)),
                ConfigConst.join(Arrays.copyOfRange(configs, idOffsetEnd + 1, configs.length)));
        }

        return new PluginProperty(configs[1], configs[2], configs[3], ConfigConst.join(Arrays.copyOfRange(configs, 4, configs.length)));
    }


    public static String requireNonEmpty(String obj, String message) {
        if (obj == null || obj.trim().isEmpty()) {
            throw new ValidateUtils.ValidException(message);
        }
        return obj.trim();
    }

    public static String buildPluginProperty(String domain, String namespace, String id, String property) {
        return String.format(PLUGIN_FORMAT, domain, namespace, id, property);
    }

    /**
     * extract config item with a fromPrefix to and convert the prefix to 'toPrefix' for configuration Compatibility
     *
     * @param cfg config source map
     * @param fromPrefix from
     * @param toPrefix to
     * @return Extracted and converted KV map
     */
    public static Map<String, String> extractAndConvertPrefix(Map<String, String> cfg, String fromPrefix, String toPrefix) {
        Map<String, String> convert = new HashMap<>();

        Set<String> keys = new HashSet<>();
        cfg.forEach((key, value) -> {
            if (key.startsWith(fromPrefix)) {
                keys.add(key);
                key = toPrefix + key.substring(fromPrefix.length());
                convert.put(key, value);
            }
        });

        // override, new configuration KV override previous KV
        convert.putAll(extractByPrefix(cfg, toPrefix));

        return convert;
    }

    /**
     * Extract config items from config by prefix
     * @param config  config
     * @param prefix  prefix
     * @return Extracted KV
     */
    public static Map<String, String> extractByPrefix(Config config, String prefix) {
        return extractByPrefix(config.getConfigs(), prefix);
    }

    public static Map<String, String> extractByPrefix(Map<String, String> cfg, String prefix) {
        Map<String, String> extract = new HashMap<>();

        // override, new configuration KV override previous KV
        cfg.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                extract.put(key, value);
            }
        });

        return extract;
    }

    public static int isChanged(String name, Map<String, String> map, String check) {
        if (map.get(name) == null || map.get(name).equals(check)) {
            return 0;
        }
        return 1;
    }
}
