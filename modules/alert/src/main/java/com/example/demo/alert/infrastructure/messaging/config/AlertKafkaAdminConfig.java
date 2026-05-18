package com.example.demo.alert.infrastructure.messaging.config;

import com.example.demo.contracts.topic.AlertTopics;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AlertKafkaAdminConfig {
    private final Environment environment;

    @Bean
    public KafkaAdmin alertKafkaAdmin() {
        String brokers = environment.getRequiredProperty("spring.kafka.bootstrap-servers");
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers));
    }

    @Bean
    public NewTopic alertFiringTopic() {
        return TopicBuilder.name(AlertTopics.ALERT_FIRING)
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }
}
