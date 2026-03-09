package com.example.demo.market_data_query.application.dto;

import java.util.List;

public record RangeView<VAL>(
        Long fromTs,
        Long toTs,
        List<VAL> values
) {
}
