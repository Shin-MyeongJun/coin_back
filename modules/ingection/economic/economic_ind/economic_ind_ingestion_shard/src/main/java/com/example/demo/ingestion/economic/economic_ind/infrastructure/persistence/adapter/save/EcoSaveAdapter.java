package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.save;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoSaveAdapter extends EconomicSaveAdapter<EcoIndEntity> {
    public EcoSaveAdapter(JpaRepository<EcoIndEntity, Long> repo) {
        super(repo);
    }

    @Override
    public void saveAll(List<EcoIndEntity> list) {
        repo.saveAll(list);
        flush();
    }
}
