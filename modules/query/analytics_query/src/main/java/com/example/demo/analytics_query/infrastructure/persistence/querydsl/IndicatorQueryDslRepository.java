package com.example.demo.analytics_query.infrastructure.persistence.querydsl;

import com.example.demo.analytics_query.application.dto.PremiumScreenerResult;
import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.TickScreenerResult;
import com.example.demo.analytics_query.infrastructure.persistence.entity.QPremiumIndicatorQueryEntity;
import com.example.demo.analytics_query.infrastructure.persistence.entity.QTickIndicatorQueryEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class IndicatorQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<TickScreenerResult> findTickByConditions(List<ScreenerCondition> conditions) {
        if (conditions.isEmpty()) return List.of();

        QTickIndicatorQueryEntity ti = QTickIndicatorQueryEntity.tickIndicatorQueryEntity;

        List<List<TickScreenerResult>> perConditionResults = conditions.stream()
                .map(c -> queryFactory
                        .select(Projections.constructor(
                                TickScreenerResult.class,
                                ti.marketCodeId, ti.interval, ti.type, ti.period, ti.value, ti.bucketCloseTs))
                        .from(ti)
                        .where(buildTickPredicate(ti, c))
                        .orderBy(ti.bucketCloseTs.desc())
                        .fetch())
                .toList();

        Set<Long> intersection = intersectMarketCodeIds(
                perConditionResults.stream().map(rows -> rows.stream()
                        .map(TickScreenerResult::marketCodeId).collect(java.util.stream.Collectors.toSet())).toList());

        return perConditionResults.get(0).stream()
                .filter(r -> intersection.contains(r.marketCodeId()))
                .toList();
    }

    public List<PremiumScreenerResult> findPremiumByConditions(List<ScreenerCondition> conditions) {
        if (conditions.isEmpty()) return List.of();

        QPremiumIndicatorQueryEntity pi = QPremiumIndicatorQueryEntity.premiumIndicatorQueryEntity;

        List<List<PremiumScreenerResult>> perConditionResults = conditions.stream()
                .map(c -> queryFactory
                        .select(Projections.constructor(
                                PremiumScreenerResult.class,
                                pi.symbol, pi.baseExchangeId, pi.compareExchangeId,
                                pi.interval, pi.type, pi.period, pi.value, pi.bucketCloseTs))
                        .from(pi)
                        .where(buildPremiumPredicate(pi, c))
                        .orderBy(pi.bucketCloseTs.desc())
                        .fetch())
                .toList();

        Set<String> intersection = intersectPremiumKeys(
                perConditionResults.stream().map(rows -> rows.stream()
                        .map(r -> premiumKey(r.symbol(), r.baseExchangeId(), r.compareExchangeId()))
                        .collect(java.util.stream.Collectors.toSet())).toList());

        return perConditionResults.get(0).stream()
                .filter(r -> intersection.contains(premiumKey(r.symbol(), r.baseExchangeId(), r.compareExchangeId())))
                .toList();
    }

    private BooleanBuilder buildTickPredicate(QTickIndicatorQueryEntity ti, ScreenerCondition c) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(ti.interval.eq(c.interval()));
        predicate.and(ti.type.eq(c.type()));
        if (c.period() != null) predicate.and(ti.period.eq(c.period()));
        if (c.minValue() != null) predicate.and(ti.value.goe(c.minValue()));
        if (c.maxValue() != null) predicate.and(ti.value.loe(c.maxValue()));
        return predicate;
    }

    private BooleanBuilder buildPremiumPredicate(QPremiumIndicatorQueryEntity pi, ScreenerCondition c) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(pi.interval.eq(c.interval()));
        predicate.and(pi.type.eq(c.type()));
        if (c.period() != null) predicate.and(pi.period.eq(c.period()));
        if (c.minValue() != null) predicate.and(pi.value.goe(c.minValue()));
        if (c.maxValue() != null) predicate.and(pi.value.loe(c.maxValue()));
        return predicate;
    }

    private Set<Long> intersectMarketCodeIds(List<Set<Long>> sets) {
        Set<Long> result = new HashSet<>(sets.get(0));
        for (int i = 1; i < sets.size(); i++) result.retainAll(sets.get(i));
        return result;
    }

    private Set<String> intersectPremiumKeys(List<Set<String>> sets) {
        Set<String> result = new HashSet<>(sets.get(0));
        for (int i = 1; i < sets.size(); i++) result.retainAll(sets.get(i));
        return result;
    }

    private String premiumKey(String symbol, Long baseExchangeId, Long compareExchangeId) {
        return symbol + ":" + baseExchangeId + ":" + compareExchangeId;
    }
}
