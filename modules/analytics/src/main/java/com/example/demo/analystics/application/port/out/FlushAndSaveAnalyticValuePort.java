package com.example.demo.analystics.application.port.out;

import java.util.List;

public interface FlushAndSaveAnalyticValuePort<ENTITY> {
    void flush();
    void saveAll(List<ENTITY> list);
}
