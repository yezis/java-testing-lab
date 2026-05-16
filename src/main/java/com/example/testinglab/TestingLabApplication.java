package com.example.testinglab;

import com.example.testinglab.common.config.ApplicationInfoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ApplicationInfoProperties.class)
@SpringBootApplication
public class TestingLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingLabApplication.class, args);
    }
}
