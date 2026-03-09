package com.example.demo.meta_data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
@EntityScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
public class MetaDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaDataApplication.class, args);
    }
}
