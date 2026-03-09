package com.example.demo.infra_heartbeat.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public record Health (
    HealthMeta meta,
    HealthValue value
){}

