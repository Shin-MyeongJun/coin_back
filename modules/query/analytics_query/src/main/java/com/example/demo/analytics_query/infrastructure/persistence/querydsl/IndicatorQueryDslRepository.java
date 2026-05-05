package com.example.demo.analytics_query.infrastructure.persistence.querydsl;

import com.example.demo.analytics_query.application.dto.ScreenerResult;
import com.example.demo.analytics_query.infrastructure.persistence.entity.QTickIndicatorQueryEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IndicatorQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<ScreenerResult> findByIndicatorCondition(String interval, String type, BigDecimal minValue, BigDecimal maxValue) {
        QTickIndicatorQueryEntity ti = QTickIndicatorQueryEntity.tickIndicatorQueryEntity;
        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(ti.interval.eq(interval));
        predicate.and(ti.type.eq(type));
        if (minValue != null) predicate.and(ti.value.goe(minValue));
        if (maxValue != null) predicate.and(ti.value.loe(maxValue));

        return queryFactory
                .select(Projections.constructor(
                        ScreenerResult.class,
                        ti.marketCodeId,
                        ti.interval,
                        ti.type,
                        ti.period,
                        ti.value,
                        ti.bucketCloseTs
                ))
                .from(ti)
                .where(predicate)
                .orderBy(ti.bucketCloseTs.desc())
                .fetch();
    }
}
