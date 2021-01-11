package ru.craftysoft.util.module.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class Jackson {

    private final ObjectMapper objectMapper;

    public String toString(Object object) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при записи объекта (" + object.getClass() + ") в строку", e);
        }
    }

    public byte[] toByteArray(Object object) {
        try {
            return this.objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при записи объекта (" + object.getClass() + ") в поток байт", e);
        }
    }


    public <T> T read(InputStream is, TypeReference<T> type) {
        try {
            return this.objectMapper.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T read(byte[] bytes, TypeReference<T> type) {
        try {
            return this.objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T read(InputStream is, Class<T> type) {
        try {
            return this.objectMapper.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T read(byte[] bytes, Class<T> type) {
        try {
            return this.objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
