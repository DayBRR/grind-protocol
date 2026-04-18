package com.davidrr.grindprotocol.integration;

import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer {

    private static final PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("grindprotocol_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        CONTAINER.start();
    }

    private PostgresTestContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }
}