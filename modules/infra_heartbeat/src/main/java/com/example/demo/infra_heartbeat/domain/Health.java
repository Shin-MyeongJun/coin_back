package com.example.demo.infra_heartbeat.domain;


public record Health (
    HealthMeta meta,
    HealthValue value
){}

