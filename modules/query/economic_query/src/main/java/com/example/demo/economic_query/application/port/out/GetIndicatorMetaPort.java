package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;

import java.util.List;
import java.util.Optional;

public interface GetIndicatorMetaPort {
    Optional<IndicatorMetaView> findById(Long id);
    List<IndicatorMetaView> findAll();
}
