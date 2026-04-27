package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.save;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoScheduleSaveAdapter extends  EconomicSaveAdapter<EconomicScheduleEntity>  {

    private final EcoScheduleCache cache;
    private final EntityToDomain<EconomicScheduleEntity, EconomicSchedule> mapper;

    public EcoScheduleSaveAdapter(JpaRepository<EconomicScheduleEntity, Long> repo, EcoScheduleCache cache, EntityToDomain<EconomicScheduleEntity, EconomicSchedule> mapper) {
        super(repo);
        this.cache = cache;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<EconomicScheduleEntity> list) {
        repo.saveAll(list).forEach(sh->{
            cache.put(mapper.toDomain(sh), sh.getId());
        });
        flush();
    }
}
