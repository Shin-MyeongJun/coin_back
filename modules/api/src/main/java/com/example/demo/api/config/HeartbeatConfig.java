package com.example.demo.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.heartbeat.enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan("com.example.demo.infra_heartbeat")
public class HeartbeatConfig {
}
