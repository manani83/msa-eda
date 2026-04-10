package com.example.hexagonal.point;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

/**
 * 포인트 모듈 테스트용 MySQL 컨테이너를 제공한다.
 */
@TestConfiguration
public class PointMySqlTestcontainersConfig {

    /**
     * 포인트 테스트가 사용할 MySQL 컨테이너를 띄운다.
     */
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("hexagonal_point_test")
                .withUsername("test")
                .withPassword("testpass");
    }
}
