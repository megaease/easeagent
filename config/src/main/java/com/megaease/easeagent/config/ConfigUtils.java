package com.megaease.easeagent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConfigUtils {
    public static String extractServiceName(Configs configs) {
        return configs.getString(ConfigConst.SERVICE_NAME);
    }

    public static String extractSystemName(Configs configs) {
        return configs.getString(ConfigConst.SYSTEM_NAME);
    }

    public static <R> void bindProp(String name, Configs configs, BiFunction<Configs, String, R> func, Consumer<R> consumer) {
        Runnable process = () -> {
            R result = func.apply(configs, name);
            consumer.accept(result);
        };
        process.run();
        configs.addChangeListener(list -> {
            boolean hasChange = list.stream().map(ChangeItem::getFullName).anyMatch(fn -> fn.equals(name));
            if (hasChange) {
                process.run();
            }
        });
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
        return prefix==null?current:ConfigConst.join(prefix,current);
    }
}
