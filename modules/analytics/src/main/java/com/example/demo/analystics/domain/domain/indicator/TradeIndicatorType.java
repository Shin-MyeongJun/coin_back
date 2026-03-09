package com.example.demo.analystics.domain.domain.indicator;


public enum TradeIndicatorType {

    //기초 지표
    MEAN("MEAN","Mean (산술평균)", TradeIndicatorGroup.STREAMING),
    STDDEV("STDDEV","Standard Deviation (표준편차)", TradeIndicatorGroup.STREAMING),
    MAX("MAX","Maximum (최대값)", TradeIndicatorGroup.STREAMING),
    MIN("MIN","Minimum (최소값)", TradeIndicatorGroup.STREAMING),
    PERCENTILE_25("PERCENTILE_25","25th Percentile (하위 25%)", TradeIndicatorGroup.SLIDING_WINDOW),
    PERCENTILE_50("PERCENTILE_50","Median (중앙값)", TradeIndicatorGroup.SLIDING_WINDOW),
    PERCENTILE_75("PERCENTILE_75","75th Percentile (상위 25%)", TradeIndicatorGroup.SLIDING_WINDOW),

    //심화 지표
    RSI("RSI","Relative Strength Index", TradeIndicatorGroup.STREAMING),
    EMA("EMA","Exponential Moving Average", TradeIndicatorGroup.STREAMING),
    MACD("MACD","Moving Average Convergence Divergence", TradeIndicatorGroup.STREAMING),
    TR("TR","True Range", TradeIndicatorGroup.STREAMING),
    ATR("ATR","Average True Range", TradeIndicatorGroup.SLIDING_WINDOW),
    SMA("SMA","Simple Moving Average", TradeIndicatorGroup.SLIDING_WINDOW),
    BOLLINGER_BANDS("BOLLINGER_BANDS","Bollinger Bands", TradeIndicatorGroup.SLIDING_WINDOW),
    STOCHASTIC("STOCHASTIC","Stochastic Oscillator", TradeIndicatorGroup.SLIDING_WINDOW);


    private final String name;
    private final String description;
    private final TradeIndicatorGroup indicatorGroup;

    TradeIndicatorType(String name,String description, TradeIndicatorGroup indicatorGroup) {
        this.name = name;
        this.description     = description;
        this.indicatorGroup  = indicatorGroup;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public TradeIndicatorGroup getIndicatorGroup() {
        return indicatorGroup;
    }




}
