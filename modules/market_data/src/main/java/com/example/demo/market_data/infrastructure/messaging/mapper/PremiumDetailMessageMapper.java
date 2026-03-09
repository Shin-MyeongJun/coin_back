package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageMapping;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.springframework.stereotype.Component;


@Component
public class PremiumDetailMessageMapper implements MessageMapping<PremiumDetail, PremiumDetailMessage> {
    @Override
    public PremiumDetailMessage toMessage(PremiumDetail pd) {
        return new PremiumDetailMessage(
                pd.symbol(),
                pd.baseExchangeId(),
                pd.compareExchangeId(),
                pd.baseBid(),
                pd.baseAsk(),
                pd.baseQuoteVal(),
                pd.compareBid(),
                pd.compareAsk(),
                pd.compareQuoteVal(),
                pd.timestamp()
        );
    }

    @Override
    public PremiumDetail toDomain(PremiumDetailMessage pdm) {
        return new PremiumDetail(
                pdm.symbol(),
                pdm.baseExchangeId(),
                pdm.compareExchangeId(),
                pdm.baseBid(),
                pdm.baseAsk(),
                pdm.baseQuoteVal(),
                pdm.compareBid(),
                pdm.compareAsk(),
                pdm.compareQuoteVal(),
                pdm.timestamp()
        );
    }
}
