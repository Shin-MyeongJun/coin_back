package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.Interval;

import java.util.List;

public interface WriteAnalyticsStatePort<DOMAIN> {
    void upsert(int partitionId,Interval ind,List<DOMAIN> domains);
}
