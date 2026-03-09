package com.example.demo.analystics.domain.domain.key;

public record IndicatorPriceDataKey<KEY extends DataKey<KEY>>(
        KEY dataKey,
        IndicatorKey indicatorKey
)implements DataKey<IndicatorPriceDataKey<KEY>> {
    @Override
    public boolean equals(IndicatorPriceDataKey<KEY> cp) {
        return cp.dataKey.equals(dataKey) && cp.indicatorKey.equals(indicatorKey);
    }
}
