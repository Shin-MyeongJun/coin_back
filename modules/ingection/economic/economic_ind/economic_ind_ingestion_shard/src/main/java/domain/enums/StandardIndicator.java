package domain.enums;

public enum StandardIndicator {
    CPI("Consumer Price Index"),
    CORE_CPI("Core Consumer Price Index"),
    BR("Base Rate"),
    FFR("Federal Funds Rate"),
    PPI("Producer Price Index"),
    PCE("Personal Consumption Expenditures"),
    CORE_PCE("Core Personal Consumption Expenditures"),
    UNRATE("Unemployment Rate"),
    T10Y2Y("10-Year Treasury Rate"),
    GDP("Gross Domestic Product");

    private final String description;

    StandardIndicator(String description) {
        this.description = description;
    }
}
