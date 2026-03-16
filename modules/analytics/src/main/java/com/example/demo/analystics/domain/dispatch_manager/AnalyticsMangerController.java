package com.example.demo.analystics.domain.dispatch_manager;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.DataKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AnalyticsMangerController<
        KEY extends DataKey<KEY>,
        VAL extends Comparable<VAL>,
        TD,
        CLOSE_TD
        > {
    void assignPartition(int id , Map<Interval, List<TD>> candles);
    void revokePartitions(Collection<Integer> partitionIds);
    List<CLOSE_TD> flush(Interval interval);
    List<TD>  get(int partitionId, Interval interval);
    void insert(int partitionId, KEY key, VAL val);
}
