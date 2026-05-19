package com.example.demo.alert.infrastructure.cache;

import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.application.usecase.AlertRulePage;
import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.Operator;
import com.example.demo.alert.domain.domain.TargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryActiveAlertRuleStoreTest {

    private StubLoadAlertRulePort loadPort;
    private InMemoryActiveAlertRuleStore sut;

    @BeforeEach
    void setUp() {
        loadPort = new StubLoadAlertRulePort();
        sut = new InMemoryActiveAlertRuleStore(loadPort);
    }

    @Test
    @DisplayName("findActive — refresh 전이면 빈 목록")
    void findActive_beforeRefresh_returnsEmpty() {
        assertThat(sut.findActive(TargetType.PREMIUM, "BTC")).isEmpty();
    }

    @Test
    @DisplayName("refresh — LoadPort에서 active rule을 받아 인덱싱")
    void refresh_indexesByTargetAndAsset() {
        AlertRule btcRule = rule(1L, TargetType.PREMIUM, "BTC", true);
        AlertRule ethRule = rule(2L, TargetType.PREMIUM, "ETH", true);
        loadPort.set(List.of(btcRule, ethRule));

        sut.refresh();

        assertThat(sut.findActive(TargetType.PREMIUM, "BTC")).containsExactly(btcRule);
        assertThat(sut.findActive(TargetType.PREMIUM, "ETH")).containsExactly(ethRule);
    }

    @Test
    @DisplayName("findActive — assetSymbol 대소문자 무시")
    void findActive_caseInsensitiveAssetSymbol() {
        AlertRule btcRule = rule(1L, TargetType.PREMIUM, "BTC", true);
        loadPort.set(List.of(btcRule));

        sut.refresh();

        assertThat(sut.findActive(TargetType.PREMIUM, "btc")).containsExactly(btcRule);
    }

    @Test
    @DisplayName("refresh — inactive rule은 제외")
    void refresh_excludesInactiveRule() {
        AlertRule active = rule(1L, TargetType.PREMIUM, "BTC", true);
        AlertRule inactive = rule(2L, TargetType.PREMIUM, "BTC", false);
        loadPort.set(List.of(active, inactive));

        sut.refresh();

        assertThat(sut.findActive(TargetType.PREMIUM, "BTC")).containsExactly(active);
    }

    @Test
    @DisplayName("initialize — @PostConstruct로 즉시 로드")
    void initialize_loadsImmediately() {
        AlertRule btcRule = rule(1L, TargetType.PREMIUM, "BTC", true);
        loadPort.set(List.of(btcRule));

        sut.initialize();

        assertThat(sut.findActive(TargetType.PREMIUM, "BTC")).containsExactly(btcRule);
    }

    @Test
    @DisplayName("refresh — LoadPort 예외 발생시 스냅샷 보존")
    void refresh_loadFailure_preservesSnapshot() {
        AlertRule btcRule = rule(1L, TargetType.PREMIUM, "BTC", true);
        loadPort.set(List.of(btcRule));
        sut.refresh();
        loadPort.fail(new RuntimeException("boom"));

        sut.refresh();

        assertThat(sut.findActive(TargetType.PREMIUM, "BTC")).containsExactly(btcRule);
    }

    @Test
    @DisplayName("findActive — null입력은 빈 목록")
    void findActive_nullInput_returnsEmpty() {
        assertThat(sut.findActive(null, "BTC")).isEmpty();
        assertThat(sut.findActive(TargetType.PREMIUM, null)).isEmpty();
    }

    private static AlertRule rule(Long id, TargetType target, String symbol, boolean active) {
        return new AlertRule(
                id,
                "user-" + id,
                "label-" + id,
                target,
                symbol,
                new AlertCondition(Operator.GT, new BigDecimal("1.0")),
                10,
                Set.of(Channel.SSE),
                active,
                0L,
                0L
        );
    }

    private static final class StubLoadAlertRulePort implements LoadAlertRulePort {
        private final AtomicReference<List<AlertRule>> source = new AtomicReference<>(List.of());
        private final AtomicReference<RuntimeException> failure = new AtomicReference<>();

        void set(List<AlertRule> rules) {
            source.set(rules);
            failure.set(null);
        }

        void fail(RuntimeException ex) {
            failure.set(ex);
        }

        @Override
        public Optional<AlertRule> findByIdForUser(long id, String userId) {
            return Optional.empty();
        }

        @Override
        public AlertRulePage findByUser(String userId, int page, int size) {
            return new AlertRulePage(List.of(), page, size, 0);
        }

        @Override
        public List<AlertRule> findAllActive() {
            RuntimeException ex = failure.get();
            if (ex != null) {
                throw ex;
            }
            return source.get();
        }

        @Override
        public List<AlertRule> findActiveByTargetAndAsset(TargetType targetType, String assetSymbol) {
            return List.of();
        }
    }
}
