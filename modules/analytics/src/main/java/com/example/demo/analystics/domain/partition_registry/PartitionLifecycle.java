package com.example.demo.analystics.domain.partition_registry;

import java.util.Collection;

public interface PartitionLifecycle {

    /** 새 파티션 할당 시 호출. 빈 상태로 초기화하거나 복원 데이터를 주입한다. */
    void assignPartition(int partitionId);

    /** 파티션 회수 시 호출. 내부 상태를 정리한다. */
    void revokePartitions(Collection<Integer> partitionIds);
}
