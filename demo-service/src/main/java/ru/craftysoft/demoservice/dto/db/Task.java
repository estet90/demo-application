package ru.craftysoft.demoservice.dto.db;

import java.time.OffsetDateTime;

public record Task(Long id,
                   String name,
                   OffsetDateTime dateTime) {
}
