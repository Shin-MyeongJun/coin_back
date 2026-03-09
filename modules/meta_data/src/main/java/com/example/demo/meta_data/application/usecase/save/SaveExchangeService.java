package com.example.demo.meta_data.application.usecase.save;

import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.application.usecase.base.SaveMetaService;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import org.springframework.stereotype.Component;

@Component
public class SaveExchangeService extends SaveMetaService<ExchangeEntity, ExchangeKey> {
    public SaveExchangeService(FindAndSavePort<ExchangeEntity, ExchangeKey> repo) {
        super(repo);
    }
}
