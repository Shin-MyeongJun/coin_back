package com.example.demo.alert.application.usecase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AlertApplicationConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
