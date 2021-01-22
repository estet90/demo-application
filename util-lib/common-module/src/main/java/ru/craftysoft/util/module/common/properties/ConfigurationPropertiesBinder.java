package ru.craftysoft.util.module.common.properties;

import java.util.Map;

import static java.util.Optional.ofNullable;

public interface ConfigurationPropertiesBinder<T> {

    String DEFAULT_NO_VALUE_ERROR_MESSAGE = "Невозможно выполнить преобразование, ни одно значение не задано";

    void bind(T object, Map<String, String> properties);

    static Integer toInt(String value, Integer defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).map(Integer::parseInt).orElse(defaultValue);
    }

    static Integer toInt(String value) {
        return ofNullable(value).map(Integer::parseInt).orElse(null);
    }

    static Long toLong(String value, Long defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).map(Long::parseLong).orElse(defaultValue);
    }

    static Long toLong(String value) {
        return ofNullable(value).map(Long::parseLong).orElse(null);
    }


    static Boolean toBoolean(String value, Boolean defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    static Boolean toBoolean(String value) {
        return ofNullable(value).map(Boolean::parseBoolean).orElse(null);
    }

    static Float toFloat(String value, Float defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).map(Float::parseFloat).orElse(defaultValue);
    }

    static Float toFloat(String value) {
        return ofNullable(value).map(Float::parseFloat).orElse(null);
    }

    static Double toDouble(String value, Double defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).map(Double::parseDouble).orElse(defaultValue);
    }

    static Double toDouble(String value) {
        return ofNullable(value).map(Double::parseDouble).orElse(null);
    }

    static String toString(String value, String defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
        return ofNullable(value).orElse(defaultValue);
    }
}
