package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;

public interface SaveOutboxRecordPort {

    void save(AnalyticsOutboxRecord record);
}
