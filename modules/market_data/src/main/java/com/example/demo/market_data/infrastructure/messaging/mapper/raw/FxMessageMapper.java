package com.example.demo.market_data.infrastructure.messaging.mapper.raw;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.domain.domain.Fx;
import org.springframework.stereotype.Component;

@Component
public class FxMessageMapper implements MessageToDomain<FxMessage, Fx> {
    @Override
    public Fx toDomain(FxMessage fm) {
        return new Fx(
                fm.base(),
                fm.compare(),
                fm.val(),
                fm.timestamp()
        );
    }
}
