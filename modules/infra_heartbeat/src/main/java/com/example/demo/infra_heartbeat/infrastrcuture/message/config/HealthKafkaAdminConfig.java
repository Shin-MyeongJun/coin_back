package com.example.demo.infra_heartbeat.infrastrcuture.message.config;

import com.example.demo.infra_heartbeat.infrastrcuture.message.HealthTopics;
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

    @Bean
    public KafkaAdmin healthKafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic heartBeatTopic() {
        return TopicBuilder.name(HealthTopics.HEARTBEAT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic healthChangeTopic() {
        return TopicBuilder.name(HealthTopics.HEALTH_CHANGE)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
