package com.example.demo.infre_exchange.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 거래소 마켓 정보 DTO
 */
public class UpbitMarketCodeDto {
    private String market;
    @JsonProperty("korean_name")
    private String koreanName;
    @JsonProperty("english_name")
    private String englishName;
    @JsonProperty("market_event")
    private MarketEvent marketEvent;

    public UpbitMarketCodeDto() {
    }

    public UpbitMarketCodeDto(String market, String koreanName, String englishName, MarketEvent marketEvent) {
        this.market = market;
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.marketEvent = marketEvent;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public MarketEvent getMarketEvent() {
        return marketEvent;
    }

    public void setMarketEvent(MarketEvent marketEvent) {
        this.marketEvent = marketEvent;
    }


    /**
     * 내·외부 이벤트 정보
     */
    public static class MarketEvent {
        private boolean warning;
        private Caution caution;

        public MarketEvent() {
        }

        public MarketEvent(boolean warning, Caution caution) {
            this.warning = warning;
            this.caution = caution;
        }

        public boolean isWarning() {
            return warning;
        }

        public void setWarning(boolean warning) {
            this.warning = warning;
        }

        public Caution getCaution() {
            return caution;
        }

        public void setCaution(Caution caution) {
            this.caution = caution;
        }

        @Override
        public String toString() {
            return "MarketEvent{" +
                   "warning=" + warning +
                   ", caution=" + caution +
                   '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MarketEvent)) return false;
            MarketEvent that = (MarketEvent) o;
            return warning == that.warning &&
                   Objects.equals(caution, that.caution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(warning, caution);
        }

        /**
         * 주의 항목
         */
        public static class Caution {
            @JsonProperty("PRICE_FLUCTUATIONS")
            private boolean priceFluctuations;
            @JsonProperty("TRADING_VOLUME_SOARING")
            private boolean tradingVolumeSoaring;
            @JsonProperty("DEPOSIT_AMOUNT_SOARING")
            private boolean depositAmountSoaring;
            @JsonProperty("GLOBAL_PRICE_DIFFERENCES")
            private boolean globalPriceDifferences;
            @JsonProperty("CONCENTRATION_OF_SMALL_ACCOUNTS")
            private boolean concentrationOfSmallAccounts;

            public Caution() {
            }

            public Caution(boolean priceFluctuations,
                           boolean tradingVolumeSoaring,
                           boolean depositAmountSoaring,
                           boolean globalPriceDifferences,
                           boolean concentrationOfSmallAccounts) {
                this.priceFluctuations = priceFluctuations;
                this.tradingVolumeSoaring = tradingVolumeSoaring;
                this.depositAmountSoaring = depositAmountSoaring;
                this.globalPriceDifferences = globalPriceDifferences;
                this.concentrationOfSmallAccounts = concentrationOfSmallAccounts;
            }

            public boolean isPriceFluctuations() {
                return priceFluctuations;
            }

            public void setPriceFluctuations(boolean priceFluctuations) {
                this.priceFluctuations = priceFluctuations;
            }

            public boolean isTradingVolumeSoaring() {
                return tradingVolumeSoaring;
            }

            public void setTradingVolumeSoaring(boolean tradingVolumeSoaring) {
                this.tradingVolumeSoaring = tradingVolumeSoaring;
            }

            public boolean isDepositAmountSoaring() {
                return depositAmountSoaring;
            }

            public void setDepositAmountSoaring(boolean depositAmountSoaring) {
                this.depositAmountSoaring = depositAmountSoaring;
            }

            public boolean isGlobalPriceDifferences() {
                return globalPriceDifferences;
            }

            public void setGlobalPriceDifferences(boolean globalPriceDifferences) {
                this.globalPriceDifferences = globalPriceDifferences;
            }

            public boolean isConcentrationOfSmallAccounts() {
                return concentrationOfSmallAccounts;
            }

            public void setConcentrationOfSmallAccounts(boolean concentrationOfSmallAccounts) {
                this.concentrationOfSmallAccounts = concentrationOfSmallAccounts;
            }

           
        }
    }
}
