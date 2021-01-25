package ru.craftysoft.util.module.common.properties.annotation;

import static java.util.Optional.ofNullable;

public class ConfigurationPropertiesBinder {

    private static final String DEFAULT_NO_VALUE_ERROR_MESSAGE = "Невозможно выполнить преобразование, ни одно значение не задано";

    public static Integer toInt(String value, Integer defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).map(Integer::parseInt).orElse(defaultValue);
    }

    public static Integer toInt(String value) {
        return ofNullable(value).map(Integer::parseInt).orElse(null);
    }

    public static Long toLong(String value, Long defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).map(Long::parseLong).orElse(defaultValue);
    }

    public static Long toLong(String value) {
        return ofNullable(value).map(Long::parseLong).orElse(null);
    }

    public static Boolean toBoolean(String value, Boolean defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    public static Boolean toBoolean(String value) {
        return ofNullable(value).map(Boolean::parseBoolean).orElse(null);
    }

    public static Float toFloat(String value, Float defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).map(Float::parseFloat).orElse(defaultValue);
    }

    public static Float toFloat(String value) {
        return ofNullable(value).map(Float::parseFloat).orElse(null);
    }

    public static Double toDouble(String value, Double defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).map(Double::parseDouble).orElse(defaultValue);
    }

    public static Double toDouble(String value) {
        return ofNullable(value).map(Double::parseDouble).orElse(null);
    }

    public static String toString(String value, String defaultValue) {
        check(value, defaultValue);
        return ofNullable(value).orElse(defaultValue);
    }

    private static void check(String value, Object defaultValue) {
        if (value == null && defaultValue == null) {
            throw new IllegalArgumentException(DEFAULT_NO_VALUE_ERROR_MESSAGE);
        }
    }
}
