package com.example.demo.market_data_query.infrastructure.persistence.mapper;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.infrastructure.persistence.entity.FxQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class FxViewMapper {

    public FxView toView(FxQueryEntity e) {
        return new FxView(e.getId(), e.getBaseCurrency(), e.getQuoteCurrency(), e.getRate(), e.getTimestamp());
    }
}
