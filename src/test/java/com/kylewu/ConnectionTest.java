package com.kylewu;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${postgres.datasource.url}")
    private String postgresUrl;

    @Value("${postgres.datasource.username}")
    private String postgresUsername;

    @Value("${postgres.datasource.password}")
    private String postgresPassword;

    @Test
    void mysqlConnectionTest() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.isValid(3)).isTrue();
            System.out.println("MySQL connected: " + conn.getMetaData().getURL());
        }
    }

    @Test
    void postgresConnectionTest() throws Exception {
        try (Connection conn = DriverManager.getConnection(postgresUrl, postgresUsername, postgresPassword);
             ResultSet rs = conn.createStatement().executeQuery("SELECT 1")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
            System.out.println("PostgreSQL connected: " + conn.getMetaData().getURL());
        }
    }

    @Test
    void redisConnectionTest() {
        String pong = redisConnectionFactory.getConnection().ping();
        assertThat(pong).isEqualTo("PONG");
        System.out.println("Redis connected, PING response: " + pong);
    }

    @Test
    void kafkaConnectionTest() throws Exception {
        Map<String, Object> configs = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers
        );
        try (AdminClient adminClient = AdminClient.create(configs)) {
            Collection<String> topics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            assertThat(topics).isNotNull();
            System.out.println("Kafka connected, topics: " + topics);
        }
    }
}
