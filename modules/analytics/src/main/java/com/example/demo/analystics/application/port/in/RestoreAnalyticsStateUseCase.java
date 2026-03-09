package com.example.demo.analystics.application.port.in;

import java.util.List;

public interface RestoreAnalyticsStateUseCase {
    void restore(List<Integer> partitionIds);
}