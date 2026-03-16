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
    public void assignPartition(int id , Map<Interval, List<TD>> candles);
    public void revokePartitions(Collection<Integer> partitionIds);
    public List<CLOSE_TD> flush(Interval interval);
    public List<TD>  get(int partitionId, Interval interval);
    public void insert(int partitionId, KEY key, VAL val);
}
