package com.example.demo.infre_exchange.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "binance")
public record BinanceProperties(
        String accessKey,
        String secretKey,
        Future future,
        Spot spot
) {
    public record Future(
            Usdt usdt
    ) {
        public record Usdt(
                String baseUrl,
                String testnetBaseUrl,
                WebSocket websocket,
                MarketData marketData,
                Trade trade
        ) {
            public record WebSocket(
                    String baseUrl,
                    String testnetUrl,
                    StreamName streamName,
                    UserStream userStream
            ) {
                public record StreamName(
                        String aggTrade,
                        String trade,
                        String kline,
                        String continuousKline,
                        String markPrice,
                        String indexPrice,
                        String miniTicker,
                        String allMiniTicker,
                        String ticker,
                        String allTicker,
                        String bookTicker,
                        String allBookTicker,
                        String depth,
                        String depth5,
                        String depth10,
                        String depth20,
                        String depth100ms
                ) {}

                public record UserStream(
                        String listenKey
                ) {}
            }

            public record MarketData(
                    String ping,
                    String time,
                    String exchangeInfo,
                    String orderBook,
                    String recentTrades,
                    String historicalTrades,
                    String aggTrades,
                    String klines,
                    String ticker24hr,
                    String priceTicker,
                    String bookTicker,
                    String premiumIndex,
                    String fundingRate,
                    String openInterest,
                    String openInterestHistory,
                    String topAccountRatio,
                    String topPositionRatio,
                    String globalLongShort,
                    String takerBuySellVolume,
                    String leverageBracket
            ) {}

            public record Trade(
                    String newOrder,
                    String queryOrder,
                    String openOrders,
                    String allOrders,
                    String batchOrders,
                    String feeBurnStatus,
                    String toggleFeeBurn,
                    String insuranceBalance,
                    String constituents
            ) {}
        }
    }

    public record Spot(
            String baseUrl,
            Map<String, String> altBaseUrls,
            General general,
            MarketData marketData,
            Account account,
            Trade trade,
            UserStream userStream
    ) {
        public record General(
                String ping,
                String time,
                String exchangeInfo
        ) {}

        public record MarketData(
                String depth,
                String recentTrades,
                String historicalTrades,
                String aggTrades,
                String klines,
                String avgPrice,
                String ticker24hr,
                String priceTicker,
                String bookTicker,
                String uiKlines
        ) {}

        public record Account(
                String info,
                String commission,
                String rateLimitOrder,
                String preventedMatches,
                String allocations,
                String orderAmendments
        ) {}

        public record Trade(
                String newOrder,
                String testOrder,
                String cancelOrder,
                String queryOrder,
                String openOrders,
                String allOrders,
                String ocoOrder,
                String cancelOco,
                String queryOco
        ) {}

        public record UserStream(
                String baseUrl,
                ListenKey listenKey
        ) {
            public record ListenKey(
                    String create,
                    String keepAlive,
                    String close
            ) {}
        }
    }
}

