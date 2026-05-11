package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.economic.EconomicIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import org.springframework.stereotype.Component;

@Component
public class EcoIndMessageMapper implements DomainToMessage<EconomicRawIndicator, EconomicIndicatorMessage> {

    @Override
    public EconomicIndicatorMessage toMessage(EconomicRawIndicator domain) {
        EconomicIndicatorCode code = domain.code();
        EconomicIndicatorValue value = domain.value();
        return new EconomicIndicatorMessage(
                code.indicatorCode(),
                code.type(),
                code.country(),
                value.value(),
                value.observationDate(),
                value.releaseDate(),
                value.timestamp(),
                "economic-ind"
        );
    }
}
