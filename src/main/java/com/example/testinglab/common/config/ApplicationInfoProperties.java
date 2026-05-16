package com.example.testinglab.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "testing-lab.application")
public record ApplicationInfoProperties(
        String displayName
) {
}
