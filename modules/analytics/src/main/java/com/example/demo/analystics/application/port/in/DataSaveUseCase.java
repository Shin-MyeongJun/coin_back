package com.example.demo.analystics.application.port.in;

import java.util.List;

public interface DataSaveUseCase<DATA> {
    void save(List<DATA> data);
}
