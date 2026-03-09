package com.example.demo.infra_heartbeat.infrastrcuture.message.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class HealthKafkaAdminConfig {

    @Value("${app.moduleName}")
    private String moduleName;

    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic heartBeatTopic() {
        return TopicBuilder.name("%s.heartbeat".formatted(moduleName))
                .partitions(1)    // 원하는 파티션 개수
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic healthChangeTopic() {
        return TopicBuilder.name("%s.healthChange".formatted(moduleName))
                .partitions(1)    // 원하는 파티션 개수
                .replicas(1)
                .build();
    }
}
