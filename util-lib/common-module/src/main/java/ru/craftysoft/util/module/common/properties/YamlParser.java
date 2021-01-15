package ru.craftysoft.util.module.common.properties;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class YamlParser {
    @Nonnull
    public static Map<String, String> parseYaml(@Nullable InputStream is) {
        if (is == null) {
            return Map.of();
        }
        var object = new Load(LoadSettings.builder().build()).loadFromInputStream(is);
        return YamlParser.parseProperties(object)
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }


    private static List<Map.Entry<String, String>> parseProperties(Object object) {
        if (object == null) {
            return List.of();
        }
        if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) object;
            return map.entrySet().stream()
                    .flatMap(entry -> YamlParser.parseProperties(entry.getValue()).stream()
                            .map(nestedEntry -> Map.entry(key(entry, nestedEntry), nestedEntry.getValue()))
                    )
                    .collect(Collectors.toList());
        }
        if (object instanceof List) {
            var list = (List<?>) object;
            var result = new ArrayList<Map.Entry<String, String>>();
            int bound = list.size();
            for (int i = 0; i < bound; i++) {
                var entry = Map.entry(String.format("[%d]", i), list.get(i));
                for (var nestedEntry : YamlParser.parseProperties(entry.getValue())) {
                    result.add(Map.entry(key(entry, nestedEntry), nestedEntry.getValue()));
                }
            }
            return result;
        }
        if (object instanceof Set) {
            var result = new ArrayList<Map.Entry<String, String>>();
            var set = (Set<?>) object;
            var iterator = set.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                var value = iterator.next();
                var entry = Map.entry(String.format("[%d]", i), value);
                for (var nestedEntry : YamlParser.parseProperties(value)) {
                    var stringStringEntry = Map.entry(key(entry, nestedEntry), nestedEntry.getValue());
                    result.add(stringStringEntry);
                }
            }
            return result;
        }
        return List.of(Map.entry("", object.toString()));
    }

    private static String key(Map.Entry<?, ?> rootEntry, Map.Entry<?, ?> nestedEntry) {
        var nestedKey = nestedEntry.getKey().toString();
        var rootKey = rootEntry.getKey().toString();
        if (nestedKey.isEmpty()) {
            return rootKey;
        }
        if (rootKey.isEmpty()) {
            return nestedKey;
        }
        if (nestedKey.startsWith("[")) {
            return rootKey + nestedKey;
        }
        return rootKey + "." + nestedKey;
    }
}
