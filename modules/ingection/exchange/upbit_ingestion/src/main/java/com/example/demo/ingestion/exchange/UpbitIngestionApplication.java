package com.example.demo.ingestion.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
public class UpbitIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(UpbitIngestionApplication.class, args);
    }

}
