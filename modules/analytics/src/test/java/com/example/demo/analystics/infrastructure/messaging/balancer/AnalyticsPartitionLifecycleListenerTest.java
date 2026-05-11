package com.example.demo.analystics.infrastructure.messaging.balancer;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AnalyticsPartitionLifecycleListenerTest {

    @Test
    void onPartitionsAssigned_callsOnlyConfiguredRestoreUseCase() {
        RestoreAnalyticsStateUseCase restore = mock(RestoreAnalyticsStateUseCase.class);
        RevokeAnalyticsStateUseCase revoke = mock(RevokeAnalyticsStateUseCase.class);
        AnalyticsPartitionLifecycleListener listener =
                new AnalyticsPartitionLifecycleListener("tick", restore, revoke);

        listener.onPartitionsAssigned(mockConsumer(), List.of(
                new TopicPartition("market-data.tick", 0),
                new TopicPartition("market-data.tick", 2)
        ));

        verify(restore).restore(List.of(0, 2));
        verifyNoInteractions(revoke);
    }

    @Test
    void onPartitionsRevokedBeforeCommit_callsOnlyConfiguredRevokeUseCase() {
        RestoreAnalyticsStateUseCase restore = mock(RestoreAnalyticsStateUseCase.class);
        RevokeAnalyticsStateUseCase revoke = mock(RevokeAnalyticsStateUseCase.class);
        AnalyticsPartitionLifecycleListener listener =
                new AnalyticsPartitionLifecycleListener("premium", restore, revoke);

        listener.onPartitionsRevokedBeforeCommit(mockConsumer(), List.of(
                new TopicPartition("market-data.premium", 1)
        ));

        verify(revoke).revoke(List.of(1));
        verifyNoInteractions(restore);
    }

    @SuppressWarnings("unchecked")
    private Consumer<Object, Object> mockConsumer() {
        return mock(Consumer.class);
    }
}
