package com.example.demo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
        "com.example.demo.api",
        "com.example.demo.analytics_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.market_data_query",
        "com.example.demo.economic_query",
        "com.example.demo.infra_shard",
        "com.example.demo.user",
        "com.example.demo.alert"
})
@AutoConfigurationPackage(basePackages = {
        "com.example.demo.api",
        "com.example.demo.analytics_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.market_data_query",
        "com.example.demo.economic_query",
        "com.example.demo.user",
        "com.example.demo.alert"
})
@ConfigurationPropertiesScan(basePackages = "com.example.demo.alert")
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
