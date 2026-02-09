package com.example.hexagonal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class OrderMySqlTestcontainersConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("hexagonal_test")
                .withUsername("test")
                .withPassword("testpass");
    }
}
