package com.example.demo.analystics.infrastructure.messaging.balancer;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j// 운영 환경에서 리밸런싱 과정을 추적하기 위해 로깅은 필수입니다.
@Component
@RequiredArgsConstructor
public class AnalyticsPartitionLifecycleListener implements ConsumerAwareRebalanceListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsPartitionLifecycleListener.class);
    private final List<RestoreAnalyticsStateUseCase> restoreUseCases;
    private final List<RevokeAnalyticsStateUseCase> revokeUseCases;

    @Override
    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        if (partitions == null || partitions.isEmpty()) {
            return;
        }

        List<Integer> partitionIds = extractPartitionIds(partitions);
        log.info("[Rebalance] 파티션 회수 감지 (Revoke). 대상 파티션: {}. 잔여 상태를 저장하고 매니저를 제거합니다.", partitionIds);

        try {
            // 파티션을 뺏기기 직전, 메모리에 남은 최신 상태를 Redis에 강제 저장(Flush)하고 Registry에서 비웁니다.
            revokeUseCases.forEach(revoker->{
                revoker.revoke(partitionIds);
            });
            log.info("[Rebalance] 파티션 {} 상태 저장 및 정리 완료.", partitionIds);
        } catch (Exception e) {
            // [주의] 여기서 예외가 터져도 카프카 컨슈머 자체가 죽지 않도록 방어해야 합니다.
            log.error("[Rebalance] 파티션 {} 상태 저장 중 치명적 오류 발생! (데이터 일부 유실 가능성)", partitionIds, e);
        }
    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        if (partitions == null || partitions.isEmpty()) {
            return;
        }

        List<Integer> partitionIds = extractPartitionIds(partitions);
        log.info("[Rebalance] 새 파티션 할당 감지 (Assign). 대상 파티션: {}. 과거 상태를 복원합니다.", partitionIds);

        try {
            // 새 파티션을 할당받았으므로, Redis에서 과거 상태를 퍼와서 매니저를 갓 구워냅니다(초기화).
            // 이 메서드가 완전히 끝날 때까지 카프카는 실시간 데이터를 가져오지 않고 대기합니다!
            restoreUseCases.forEach(restorer->{
                restorer.restore(partitionIds);
            });
            log.info("[Rebalance] 파티션 {} 상태 복원 및 매니저 준비 완료. 실시간 컨슘을 재개합니다.", partitionIds);
        } catch (Exception e) {
            log.error("[Rebalance] 파티션 {} 상태 복원 중 치명적 오류 발생! (빈 상태로 시작될 수 있음)", partitionIds, e);
        }
    }

    /**
     * 카프카의 TopicPartition 객체 모음에서 순수 파티션 번호(Integer)만 추출하는 헬퍼 메서드
     */
    private List<Integer> extractPartitionIds(Collection<TopicPartition> partitions) {
        return partitions.stream()
                .map(TopicPartition::partition)
                .toList(); // Java 16 이상 기준. 15 이하라면 .collect(Collectors.toList()) 사용
    }
}
