package com.example.demo.infre_exchange.upbit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "upbit")  // application.yml의 upbit: 항목 바인딩 :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
@Data
public class UpbitProperties {
    private String baseUrl;


    private String accessKey;   
    private String secretKey;     
    
    private Websockets websockets;
    private Quotation quotation;
    private Accounts accounts;
    private Orders orders;
    private Withdraws withdraws;
    private Deposits deposits;
    
    @Data
    public static class Websockets{
        private String publicUrl;
        private String privateUrl;
    }
    

    @Data
    public static class Quotation {
        private String marketsAll;
        private String ticker;
        private String orderbook;
        private String tradesTicks;
        private Candles candles;
        @Data
        public static class Candles {
            private String minutes;
            private String days;
            private String weeks;
            private String months;
        }
    }

    @Data
    public static class Accounts {
        private String list;
    }

    @Data
    public static class Orders {
        private String create;
        private String cancel;
        private String list;
        private String chance;
    }

    @Data
    public static class Withdraws {
        private String chance;
        private String list;
        private String create;
    }

    @Data
    public static class Deposits {
        private String chance;
        private String list;
    }
}
