package com.example.demo.analystics.infrastructure.messaging.balancer;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;

import java.util.Collection;
import java.util.List;

public class AnalyticsPartitionLifecycleListener implements ConsumerAwareRebalanceListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsPartitionLifecycleListener.class);

    private final String streamName;
    private final RestoreAnalyticsStateUseCase restoreUseCase;
    private final RevokeAnalyticsStateUseCase revokeUseCase;

    public AnalyticsPartitionLifecycleListener(
            String streamName,
            RestoreAnalyticsStateUseCase restoreUseCase,
            RevokeAnalyticsStateUseCase revokeUseCase
    ) {
        this.streamName = streamName;
        this.restoreUseCase = restoreUseCase;
        this.revokeUseCase = revokeUseCase;
    }

    @Override
    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        if (partitions == null || partitions.isEmpty()) {
            return;
        }

        List<Integer> partitionIds = extractPartitionIds(partitions);
        log.info("[Rebalance:{}] Partitions revoked: {}. Saving state and clearing registry.",
                streamName,
                partitionIds);

        try {
            revokeUseCase.revoke(partitionIds);
            log.info("[Rebalance:{}] Partition state saved and registry cleared: {}.",
                    streamName,
                    partitionIds);
        } catch (Exception e) {
            log.error("[Rebalance:{}] Failed to save partition state: {}.",
                    streamName,
                    partitionIds,
                    e);
        }
    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        if (partitions == null || partitions.isEmpty()) {
            return;
        }

        List<Integer> partitionIds = extractPartitionIds(partitions);
        log.info("[Rebalance:{}] Partitions assigned: {}. Restoring state.",
                streamName,
                partitionIds);

        try {
            restoreUseCase.restore(partitionIds);
            log.info("[Rebalance:{}] Partition state restored: {}.",
                    streamName,
                    partitionIds);
        } catch (Exception e) {
            log.error("[Rebalance:{}] Failed to restore partition state: {}.",
                    streamName,
                    partitionIds,
                    e);
        }
    }

    private List<Integer> extractPartitionIds(Collection<TopicPartition> partitions) {
        return partitions.stream()
                .map(TopicPartition::partition)
                .toList();
    }
}
