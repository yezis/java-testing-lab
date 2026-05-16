package com.example.testinglab.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class ApplicationInfoPropertiesTest {

    @Autowired
    private ApplicationInfoProperties applicationInfoProperties;

    @Test
    void shouldLoadTestProfileConfiguration() {
        String displayName = applicationInfoProperties.displayName();

        assertThat(displayName).isEqualTo("Java Testing Lab Test");
    }

}