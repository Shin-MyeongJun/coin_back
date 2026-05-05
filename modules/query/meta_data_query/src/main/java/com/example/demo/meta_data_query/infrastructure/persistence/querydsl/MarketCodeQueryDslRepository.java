package com.example.demo.meta_data_query.infrastructure.persistence.querydsl;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;
import com.example.demo.meta_data_query.infrastructure.persistence.entity.QMarketCodeQueryEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MarketCodeQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<MarketCodeSearchResult> search(String keyword, Long exchangeId) {
        QMarketCodeQueryEntity mc = QMarketCodeQueryEntity.marketCodeQueryEntity;
        BooleanBuilder predicate = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            predicate.and(
                    mc.tradingPair.containsIgnoreCase(keyword)
                            .or(mc.base.containsIgnoreCase(keyword))
            );
        }
        if (exchangeId != null) {
            predicate.and(mc.exchangeId.eq(exchangeId));
        }

        return queryFactory
                .select(Projections.constructor(
                        MarketCodeSearchResult.class,
                        mc.id,
                        mc.exchangeId,
                        mc.tradingPair,
                        mc.base,
                        mc.quote
                ))
                .from(mc)
                .where(predicate)
                .fetch();
    }
}
