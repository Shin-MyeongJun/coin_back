package com.example.demo.meta_data.application.usecase.save;

import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.application.usecase.base.SaveMetaService;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import org.springframework.stereotype.Component;

@Component
public class SaveMarketCodeService extends SaveMetaService<MarketCodeEntity, MarketCodeKey> {
    public SaveMarketCodeService(FindAndSavePort<MarketCodeEntity, MarketCodeKey> repo) {
        super(repo);
    }
}
