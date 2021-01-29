package ru.craftysoft.demoservice.dto.db;

public record User(Long id,
                   String name,
                   String login,
                   String password) {
}
