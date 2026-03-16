package com.example.demo.analystics.domain.dispatch_manager;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.manager.indicator.IndicatorManager;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class IndicatorMangerController<KEY extends DataKey<KEY>,
        IND extends OpenTradeIndicator<KEY>,
        CLOSE_IND,
        MANAGER extends IndicatorManager<KEY,IND,CLOSE_IND>
        > implements AnalyticsMangerController<KEY,BigDecimal,IND,CLOSE_IND> {
    // Manager 상태를 관리하는 핵심 저장소
    protected final Map<Integer, MANAGER> managerMap = new ConcurrentHashMap<>();


    // ----------------------------------------------------
    // [1] restore or revoke
    // ----------------------------------------------------
    public void assignPartition(int id ,Map<Interval, List<IND>> inds) {
            MANAGER manager = createManager();
            manager.assign(inds);
            managerMap.put(id,manager);
    }
    public void revokePartitions(Collection<Integer> partitionIds) {
        partitionIds.forEach(managerMap::remove);
    }
    // ----------------------------------------------------
    // [2] flush And Get
    // ----------------------------------------------------
    public List<CLOSE_IND> flush(Interval interval) {
        return   managerMap.values().stream()
                .flatMap(manager -> manager.drain(interval).stream())
                .toList();
    }
    public List<IND>  get(int partitionId, Interval interval) {
        MANAGER manager = getManager(partitionId);
        return manager.getInds(interval);
    }
    // ----------------------------------------------------
    // [3] insert
    // ----------------------------------------------------
    public void insert(int partitionId, KEY key, BigDecimal val) {
        MANAGER manager = managerMap.get(partitionId);
        if (manager != null) {
            manager.update(key, val);
        }else{
            log.info("Insert candidate for partition {} failed:: no manager", partitionId);
        }
    }



    // Dispatcher가 Manager를 꺼내 쓸 수 있도록 열어주는 창구
    private MANAGER getManager(int partitionId) {
        return managerMap.get(partitionId);
    }

    protected abstract MANAGER createManager();
}
