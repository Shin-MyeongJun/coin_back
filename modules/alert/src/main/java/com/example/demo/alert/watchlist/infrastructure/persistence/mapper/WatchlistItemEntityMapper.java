package com.example.demo.alert.watchlist.infrastructure.persistence.mapper;

import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;
import com.example.demo.alert.watchlist.infrastructure.persistence.entity.WatchlistItemEntity;
import com.example.demo.infra_shard.persistence.EntityMapping;
import org.springframework.stereotype.Component;

@Component
public class WatchlistItemEntityMapper implements EntityMapping<WatchlistItem, WatchlistItemEntity> {

    @Override
    public WatchlistItemEntity toEntity(WatchlistItem domain) {
        return WatchlistItemEntity.builder()
                .id(domain.id())
                .userId(domain.userId())
                .marketCodeId(domain.marketCodeId())
                .symbol(domain.symbol())
                .domesticExchangeId(domain.domesticExchangeId())
                .offshoreExchangeId(domain.offshoreExchangeId())
                .displayOrder(domain.displayOrder())
                .memo(domain.memo())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }

    @Override
    public WatchlistItem toDomain(WatchlistItemEntity entity) {
        return new WatchlistItem(
                entity.getId(),
                entity.getUserId(),
                entity.getMarketCodeId(),
                entity.getSymbol(),
                entity.getDomesticExchangeId(),
                entity.getOffshoreExchangeId(),
                entity.getDisplayOrder(),
                entity.getMemo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
