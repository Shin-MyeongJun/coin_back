package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;

import java.util.List;

public interface LoadPendingOutboxPort {

    List<AnalyticsOutboxRecord> loadPending(int batchSize, int maxRetry);
}
