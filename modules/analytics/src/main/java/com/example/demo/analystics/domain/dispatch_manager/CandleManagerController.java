package com.example.demo.analystics.domain.dispatch_manager;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.CloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.manager.candle.CandleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class CandleManagerController<
        KEY extends DataKey<KEY>,
        VAL extends Comparable<VAL>,
        CANDLE extends OpenCandle<KEY, VAL>,
        CLOSE_CANDLE extends CloseCandle,
        MANAGER extends CandleManager<KEY, VAL, CANDLE, CLOSE_CANDLE, ?>> implements AnalyticsMangerController<KEY,VAL,CANDLE,CLOSE_CANDLE> {

    // Manager 상태를 관리하는 핵심 저장소
    protected final Map<Integer, MANAGER> managerMap = new ConcurrentHashMap<>();


    // ----------------------------------------------------
    // [1] restore or revoke
    // ----------------------------------------------------
    public void assignPartition(int id ,Map<Interval,List<CANDLE>> candles) {
            MANAGER manager = createManager();
            manager.assign(candles);
            managerMap.put(id,manager);
    }
    public void revokePartitions(Collection<Integer> partitionIds) {
        partitionIds.forEach(managerMap::remove);
    }
    // ----------------------------------------------------
    // [2] flush And Get
    // ----------------------------------------------------
    public List<CLOSE_CANDLE> flush(Interval interval) {
         return   managerMap.values().stream()
                 .flatMap(manager -> manager.drain(interval).stream())
                 .toList();
    }
    public List<CANDLE>  get(int partitionId, Interval interval) {
        List<CANDLE> list = new ArrayList<>();
        MANAGER manager = getManager(partitionId);
        return manager.getCandles(interval);
    }
    // ----------------------------------------------------
    // [3] insert
    // ----------------------------------------------------
    public void insert(int partitionId, KEY key, VAL val) {
        MANAGER manager = managerMap.get(partitionId);
        if (manager != null) {
            manager.insert(key, val);
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
