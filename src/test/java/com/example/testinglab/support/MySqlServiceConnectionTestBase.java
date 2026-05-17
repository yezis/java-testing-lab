package com.example.testinglab.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.sql.init.mode=always",
        "spring.datasource.hikari.initialization-fail-timeout=60000",
        "spring.datasource.hikari.connection-timeout=30000"
})
@Testcontainers(disabledWithoutDocker = true)
@Transactional
public abstract class MySqlServiceConnectionTestBase {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("testing_lab")
            .withUsername("test")
            .withPassword("test");
}
