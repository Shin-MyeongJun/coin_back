package com.example.demo.ingestion.economic.crawling.domain.domain.enums;

public enum GlobalIndexSymbol {
    SP500("^GSPC", "S&P 500"),
    NASDAQ("^IXIC", "NASDAQ Composite"),
    VIX("^VIX", "CBOE Volatility Index"),
    DXY("DX-Y.NYB", "US Dollar Index"),
    GOLD("GC=F", "Gold Futures"),
    CRUDE_OIL("CL=F", "Crude Oil WTI");

    public final String yahooSymbol;
    public final String displayName;

    GlobalIndexSymbol(String yahooSymbol, String displayName) {
        this.yahooSymbol = yahooSymbol;
        this.displayName = displayName;
    }
}
