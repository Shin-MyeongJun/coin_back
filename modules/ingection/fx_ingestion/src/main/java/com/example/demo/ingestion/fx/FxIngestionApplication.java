package com.example.demo.ingestion.fx;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
public class FxIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FxIngestionApplication.class, args);
    }

}
