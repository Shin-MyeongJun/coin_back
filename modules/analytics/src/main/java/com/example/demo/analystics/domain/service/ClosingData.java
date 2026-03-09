package com.example.demo.analystics.domain.service;

import com.example.demo.analystics.domain.domain.Interval;

public interface ClosingData<OPEN,CLOSE> {
    CLOSE toClose(OPEN open , Interval interval);
}
