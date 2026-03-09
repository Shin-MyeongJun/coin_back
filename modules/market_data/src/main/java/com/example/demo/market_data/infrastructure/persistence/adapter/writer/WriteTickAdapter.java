package com.example.demo.market_data.infrastructure.persistence.adapter.writer;

import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.WritePriceValueAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.springframework.stereotype.Component;

@Component
public class WriteTickAdapter extends WritePriceValueAdapter<Tick, TickEntity> {
    public WriteTickAdapter(PriceValueSaveAdapter<TickEntity> adapter, DomainToEntity<Tick,TickEntity> mapper) {
        super(adapter, mapper);
    }
}
