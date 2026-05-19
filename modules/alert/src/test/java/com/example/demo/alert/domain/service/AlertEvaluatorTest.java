package com.example.demo.alert.domain.service;

import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.AlertEvaluationResult;
import com.example.demo.alert.domain.domain.AlertMetric;
import com.example.demo.alert.domain.domain.AlertOperator;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.Operator;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertEvaluatorTest {

    private final AlertEvaluator sut = new AlertEvaluator();

    @Test
    @DisplayName("GT 매트릭스 — bid > threshold이면 BUY 매칭, ask <= threshold이면 SELL 미매칭")
    void evaluate_gt_matches_buy_only() {
        AlertRule rule = rule(Operator.GT, "1.0", "BTC");
        PremiumMessage msg = premium("BTC", "1.5", "0.9");

        List<AlertEvaluationResult> results = sut.evaluate(rule, msg);

        assertThat(results).hasSize(2);
        AlertEvaluationResult buy = findMetric(results, AlertMetric.BUY_PREMIUM_RATE);
        AlertEvaluationResult sell = findMetric(results, AlertMetric.SELL_PREMIUM_RATE);
        assertThat(buy.matched()).isTrue();
        assertThat(buy.observedValue()).isEqualByComparingTo("1.5");
        assertThat(sell.matched()).isFalse();
        assertThat(sell.observedValue()).isEqualByComparingTo("0.9");
    }

    @Test
    @DisplayName("GTE — observed == threshold이면 매칭")
    void evaluate_gte_equal_matches() {
        AlertRule rule = rule(Operator.GTE, "2.0", "BTC");
        PremiumMessage msg = premium("BTC", "2.0", "2.0");

        List<AlertEvaluationResult> results = sut.evaluate(rule, msg);

        assertThat(results).allMatch(AlertEvaluationResult::matched);
    }

    @Test
    @DisplayName("LT — bid < threshold이면 BUY 매칭")
    void evaluate_lt_matches_below_threshold() {
        AlertRule rule = rule(Operator.LT, "5.0", "BTC");
        PremiumMessage msg = premium("BTC", "4.9", "5.1");

        List<AlertEvaluationResult> results = sut.evaluate(rule, msg);

        assertThat(findMetric(results, AlertMetric.BUY_PREMIUM_RATE).matched()).isTrue();
        assertThat(findMetric(results, AlertMetric.SELL_PREMIUM_RATE).matched()).isFalse();
    }

    @Test
    @DisplayName("LTE — observed == threshold이면 매칭")
    void evaluate_lte_equal_matches() {
        AlertRule rule = rule(Operator.LTE, "3.0", "BTC");
        PremiumMessage msg = premium("BTC", "3.0", "2.5");

        List<AlertEvaluationResult> results = sut.evaluate(rule, msg);

        assertThat(results).allMatch(AlertEvaluationResult::matched);
    }

    @Test
    @DisplayName("EQ는 evaluator에서 IllegalArgumentException")
    void evaluate_eq_throws() {
        AlertRule rule = rule(Operator.EQ, "1.0", "BTC");
        PremiumMessage msg = premium("BTC", "1.0", "1.0");

        assertThatThrownBy(() -> sut.evaluate(rule, msg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EQ");
    }

    @Test
    @DisplayName("CROSSES_ABOVE.apply는 IllegalArgumentException")
    void crosses_above_throws() {
        assertThatThrownBy(() -> AlertOperator.CROSSES_ABOVE.apply(new BigDecimal("1"), new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CROSSES_ABOVE");
    }

    @Test
    @DisplayName("CROSSES_BELOW.apply는 IllegalArgumentException")
    void crosses_below_throws() {
        assertThatThrownBy(() -> AlertOperator.CROSSES_BELOW.apply(new BigDecimal("1"), new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CROSSES_BELOW");
    }

    @Test
    @DisplayName("symbol이 다르면 빈 결과")
    void evaluate_symbol_mismatch_returns_empty() {
        AlertRule rule = rule(Operator.GT, "1.0", "BTC");
        PremiumMessage msg = premium("ETH", "5.0", "5.0");

        assertThat(sut.evaluate(rule, msg)).isEmpty();
    }

    @Test
    @DisplayName("PREMIUM이 아닌 targetType이면 빈 결과")
    void evaluate_non_premium_target_returns_empty() {
        AlertRule rule = new AlertRule(
                1L,
                "user-1",
                "label",
                TargetType.TICK,
                "BTC",
                new AlertCondition(Operator.GT, new BigDecimal("1.0")),
                10,
                Set.of(Channel.SSE),
                true,
                0L,
                0L
        );
        PremiumMessage msg = premium("BTC", "5.0", "5.0");

        assertThat(sut.evaluate(rule, msg)).isEmpty();
    }

    private static AlertRule rule(Operator op, String threshold, String symbol) {
        return new AlertRule(
                1L,
                "user-1",
                "label",
                TargetType.PREMIUM,
                symbol,
                new AlertCondition(op, new BigDecimal(threshold)),
                10,
                Set.of(Channel.SSE),
                true,
                0L,
                0L
        );
    }

    private static PremiumMessage premium(String symbol, String bid, String ask) {
        return new PremiumMessage(symbol, 1L, 2L, new BigDecimal(bid), new BigDecimal(ask), 1_000L);
    }

    private static AlertEvaluationResult findMetric(List<AlertEvaluationResult> results, AlertMetric metric) {
        return results.stream().filter(r -> r.metric() == metric).findFirst().orElseThrow();
    }
}
