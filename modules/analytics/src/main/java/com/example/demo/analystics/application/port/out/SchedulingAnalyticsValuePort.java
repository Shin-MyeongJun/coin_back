package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.Interval;

public interface SchedulingAnalyticsValuePort {
    void process(Interval interval);
}
