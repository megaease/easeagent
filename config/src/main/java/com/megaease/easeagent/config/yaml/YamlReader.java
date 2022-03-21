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

package com.megaease.easeagent.config.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class YamlReader {

    private Map<String, Object> yaml;

    private static final DumperOptions DUMPER_OPTIONS;

    static {
        DUMPER_OPTIONS = new DumperOptions();
        DUMPER_OPTIONS.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
    }

    public YamlReader() {

    }

    public YamlReader load(InputStream in) {
        if (in != null) {
            yaml = new Yaml(DUMPER_OPTIONS).load(in);
        }
        return this;
    }

    public Map<String, Object> getYaml() {
        return yaml;
    }

    public static YamlReader merge(YamlReader target, YamlReader source) {
        Map<String, Object> targetMap = target.yaml;
        Map<String, Object> sourceMap = source.yaml;

        merge(targetMap, sourceMap);

        YamlReader result = new YamlReader();
        result.yaml = new HashMap<>(targetMap);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void merge(Map<String, Object> target, Map<String, Object> source) {
        source.forEach((key, value) -> {
            Object existing = target.get(key);
            if (value instanceof Map && existing instanceof Map) {
                Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) existing);
                merge(result, (Map<String, Object>) value);
                target.put(key, result);
            } else {
                target.put(key, value);
            }
        });
    }

    public Map<String, String> compress() {
        if (Objects.isNull(yaml) || yaml.size() == 0) {
            return Collections.emptyMap();
        }

        final Deque<String> keyStack = new LinkedList<>();
        final Map<String, String> resultMap = new HashMap<>();

        compress(yaml, keyStack, resultMap);

        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private void compress(Map<?, Object> result, Deque<String> keyStack, Map<String, String> resultMap) {
        result.forEach((k, v) -> {
            keyStack.addLast(String.valueOf(k));

            if (v instanceof Map) {
                compress((Map<?, Object>) v, keyStack, resultMap);
                keyStack.removeLast();
                return;
            }

            if (v instanceof List) {
                String value = ((List<Object>) v).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("."));

                resultMap.put(String.join(".", keyStack), value);
                keyStack.removeLast();
                return;
            }

            resultMap.put(String.join(".", keyStack), String.valueOf(v));
            keyStack.removeLast();
        });
    }

}
