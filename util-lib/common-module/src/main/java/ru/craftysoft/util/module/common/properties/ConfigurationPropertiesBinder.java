package ru.craftysoft.util.module.common.properties;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;

public interface ConfigurationPropertiesBinder<T> {
    void bind(T object, Map<String, String> properties);

    void bind(T object, Map.Entry<String, String> properties);

    static int toInt(Map.Entry<String, String> entry, Integer defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return Integer.parseInt(entry.getValue());
    }

    static Integer toInt(Map.Entry<String, String> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return Integer.parseInt(entry.getValue());
    }

    static long toLong(Map.Entry<String, String> entry, Long defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return Long.parseLong(entry.getValue());
    }

    static Long toLong(Map.Entry<String, String> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return Long.parseLong(entry.getValue());
    }


    static boolean toBoolean(Map.Entry<String, String> entry, Boolean defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(entry.getValue());
    }

    static Boolean toBoolean(Map.Entry<String, String> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return Boolean.parseBoolean(entry.getValue());
    }

    static float toFloat(Map.Entry<String, String> entry, Float defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return Float.parseFloat(entry.getValue());
    }

    static Float toFloat(Map.Entry<String, String> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return Float.parseFloat(entry.getValue());
    }

    static double toDouble(Map.Entry<String, String> entry, Double defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return Double.parseDouble(entry.getValue());
    }

    static Double toDouble(Map.Entry<String, String> entry) {
        if (entry.getValue() == null) {
            return null;
        }
        return Double.parseDouble(entry.getValue());
    }

    static String toString(Map.Entry<String, String> entry) {
        return entry.getValue();
    }

    static String toString(Map.Entry<String, String> entry, String defaultValue) {
        if (entry.getValue() == null && defaultValue == null) {
            throw new IllegalArgumentException("Can't set to null property without default value. Key: " + entry.getKey());
        }
        if (entry.getValue() == null) {
            return defaultValue;
        }
        return entry.getValue();
    }

    static Tuple2<String, Map.Entry<String, String>> subEntry(Map.Entry<String, String> entry) {
        var parts = entry.getKey().split("\\.");
        var firstPart = parts[0];
        var otherParts = String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        var newEntry = new java.util.AbstractMap.SimpleImmutableEntry<>(otherParts, entry.getValue());
        return Tuples.of(firstPart, newEntry);
    }

    static boolean hasParts(Map.Entry<String, String> entry) {
        return entry.getKey().contains(".");
    }
}
