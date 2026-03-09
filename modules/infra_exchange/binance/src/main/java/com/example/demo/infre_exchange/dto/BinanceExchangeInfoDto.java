package com.example.demo.infre_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BinanceExchangeInfoDto {

    private String timezone;
    private Long serverTime;
    private List<ExchangeFilter> exchangeFilters;
    private List<RateLimit> rateLimits;
    private List<Asset> assets;
    private List<Symbol> symbols;

    @Getter
    @NoArgsConstructor
    public static class ExchangeFilter {
        // 구조가 없거나 빈 배열로만 들어오므로 생략 or 확장 가능
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimit {
        private String interval;
        private Integer intervalNum;
        private Integer limit;
        private String rateLimitType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Asset {
        private String asset;
        private Boolean marginAvailable;
        private String autoAssetExchange;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Symbol {
        private String symbol;
        private String pair;
        private String contractType;
        private Long deliveryDate;
        private Long onboardDate;
        private String status;
        private String maintMarginPercent;
        private String requiredMarginPercent;
        private String baseAsset;
        private String quoteAsset;
        private String marginAsset;
        private Integer pricePrecision;
        private Integer quantityPrecision;
        private Integer baseAssetPrecision;
        private Integer quotePrecision;
        private String underlyingType;
        private List<String> underlyingSubType;
        private Integer settlePlan;
        private String triggerProtect;
        private List<Filter> filters;
        private List<String> OrderType;
        private List<String> timeInForce;
        private String liquidationFee;
        private String marketTakeBound;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String filterType;
        private String minPrice;
        private String maxPrice;
        private String tickSize;
        private String minQty;
        private String maxQty;
        private String stepSize;
        private Integer limit;
        private String notional;
        private String multiplierUp;
        private String multiplierDown;
        private String multiplierDecimal;
        private String positionControlSide;
    }
}