package com.example.demo.ingestion.exchange;


import com.example.demo.infre_exchange.config.BinanceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
@EnableConfigurationProperties(BinanceProperties.class)
public class BinanceIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(BinanceIngestionApplication.class, args);
    }

}
