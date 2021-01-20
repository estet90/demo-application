package ru.craftysoft.util.module.common.properties.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class YamlPropertySource implements PropertySource {
    public static Logger log = LoggerFactory.getLogger(YamlPropertySource.class);

    private final String configFileLocation;

    public YamlPropertySource(String configFileLocation) {
        this.configFileLocation = configFileLocation;
    }

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
        } catch (java.nio.file.NoSuchFileException e) {
            log.warn("Config file location doesn't exist: {}", this.configFileLocation);
            return Map.of();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
