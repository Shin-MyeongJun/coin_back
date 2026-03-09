package com.example.demo.analystics.application.kernel.base;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.manager.indicator.IndicatorManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DispatchIndicatorManager<KEY extends DataKey<KEY>,
        IND extends OpenTradeIndicator<KEY>,
        CLOSE_IND,
        MANAGER extends IndicatorManager<KEY,IND,CLOSE_IND>
        >

    {
        protected final Map<Integer,MANAGER> managerMap = new ConcurrentHashMap<>();
        private final WriteAnalyticsValuePort<CLOSE_IND> dataSaveUseCase;

        protected DispatchIndicatorManager(WriteAnalyticsValuePort<CLOSE_IND> dataSaveUseCase) {
            this.dataSaveUseCase = dataSaveUseCase;
        }


        public void dispatch(int partitionId, KEY key, BigDecimal val) {
            MANAGER manager = managerMap.computeIfAbsent(partitionId, pid -> createManager());
            manager.update(key, val);
        }


        public void flush(Interval interval){
            List<CLOSE_IND> indices = new ArrayList<>();
            managerMap.forEach((k, v) -> {
                indices.addAll(v.drain(interval));
            });
            dataSaveUseCase.write(indices);
        }

        protected abstract MANAGER createManager();
    }
