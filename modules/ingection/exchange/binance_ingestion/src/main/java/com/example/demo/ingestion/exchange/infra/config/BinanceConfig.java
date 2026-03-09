package com.example.demo.ingestion.exchange.infra.config;


import com.example.demo.infre_exchange.client.impl.BinanceFutureConnector;
import com.example.demo.infre_exchange.config.BinanceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinanceConfig {
    @Bean
    public BinanceFutureConnector binanceFutureConnector(
            BinanceProperties props) {
        return new BinanceFutureConnector(props);
    }
}
