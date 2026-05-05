package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;

import java.util.List;

public interface GetIndicatorListByCategoryPort {
    List<IndicatorMetaView> findByType(String type);
}
