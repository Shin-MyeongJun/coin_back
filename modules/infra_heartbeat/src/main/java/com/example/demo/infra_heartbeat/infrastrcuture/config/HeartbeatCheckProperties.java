package com.example.demo.infra_heartbeat.infrastrcuture.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.heartbeat")
public record HeartbeatCheckProperties(List<String> modules) {}
