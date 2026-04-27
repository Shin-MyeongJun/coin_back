package com.example.demo.ingestion.economic.economic_ind.domain.enums;

public enum StandardIndicator {
    // === 물가 (Inflation) ===
    CPI("Consumer Price Index"),
    CORE_CPI("Core Consumer Price Index"),
    PPI("Producer Price Index"),
    PCE("Personal Consumption Expenditures"),
    CORE_PCE("Core Personal Consumption Expenditures"),

    // === 금리 및 채권 (Interest Rates & Yields) ===
    BR("Base Rate"),
    FFR("Federal Funds Rate"),
    T10Y("10-Year Treasury Rate"),
    T2Y("2-Year Treasury Rate"),
    T10Y2Y("10-Year and 2-Year Treasury Yield Spread"),

    // === 고용 (Employment) ===
    UNRATE("Unemployment Rate"),
    NFP("Nonfarm Payrolls"),
    JOBLESS_CLAIMS("Initial Jobless Claims"),

    // === 실물 경제 및 성장 (Real Economy & Growth) ===
    GDP("Gross Domestic Product"),
    PMI_MFG("Manufacturing Purchasing Managers' Index"),
    PMI_SRV("Services Purchasing Managers' Index"),
    RETAIL_SALES("Retail Sales"),
    IND_PROD("Industrial Production"),

    // === 통화량 (Liquidity) ===
    M2("M2 Money Supply");

    private final String description;

    StandardIndicator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}