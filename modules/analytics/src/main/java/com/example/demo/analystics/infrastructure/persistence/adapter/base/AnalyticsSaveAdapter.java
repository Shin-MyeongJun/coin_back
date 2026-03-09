package com.example.demo.analystics.infrastructure.persistence.adapter.base;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@RequiredArgsConstructor
public class AnalyticsSaveAdapter<ENTITY> implements FlushAndSaveAnalyticValuePort<ENTITY> {
    private  final JpaRepository<ENTITY, Long> repo;

    @Override
    public void flush() {
        repo.flush();
    }

    @Override
    public void saveAll(List<ENTITY> list) {
        repo.saveAll(list);
    }


}
