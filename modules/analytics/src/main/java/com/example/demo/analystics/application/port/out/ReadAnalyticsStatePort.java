package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.Interval;

import java.util.Map;

public interface ReadAnalyticsStatePort<KEY,RECOVER> {
     Map<KEY,RECOVER> read(int partitionId,Interval interval);
}
