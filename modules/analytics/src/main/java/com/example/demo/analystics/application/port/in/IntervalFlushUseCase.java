package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.Interval;

public interface IntervalFlushUseCase{
       void flush(Interval interval);
}
