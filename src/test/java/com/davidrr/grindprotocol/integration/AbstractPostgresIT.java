package com.davidrr.grindprotocol.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("integration-test")
public abstract class AbstractPostgresIT {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PostgresTestContainer.getInstance().getJdbcUrl());
        registry.add("spring.datasource.username", () -> PostgresTestContainer.getInstance().getUsername());
        registry.add("spring.datasource.password", () -> PostgresTestContainer.getInstance().getPassword());
        registry.add("spring.datasource.driver-class-name", () -> PostgresTestContainer.getInstance().getDriverClassName());
    }
}