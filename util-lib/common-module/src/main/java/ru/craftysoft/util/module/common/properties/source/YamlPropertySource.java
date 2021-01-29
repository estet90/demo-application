package ru.craftysoft.util.module.common.properties.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class YamlPropertySource implements PropertySource {

    private final String configFileLocation;

    @Override
    public Map<String, String> getProperties() {
        var isClassPath = this.configFileLocation.startsWith("classpath:/");
        var actualPath = isClassPath
                ? this.configFileLocation.substring(10)
                : this.configFileLocation;
        try (var is = isClassPath
                ? YamlPropertySource.class.getResourceAsStream(actualPath)
                : Files.newInputStream(Paths.get(actualPath))) {
            if (is == null) {
                log.warn("Config file location doesn't exist: {}", this.configFileLocation);
                return Map.of();
            } else {
                return YamlParser.parseYaml(is);
            }
        } catch (NoSuchFileException e) {
            log.warn("Config file location doesn't exist: {}", this.configFileLocation);
            return Map.of();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
