package infrastructure.persistence.adapter.save;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import domain.EconomicIndicatorCode;
import infrastructure.cache.EcoIndCodeCache;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoCodeSaveAdapter extends EconomicSaveAdapter<EcoIndCodeEntity> {
    
    private final EcoIndCodeCache codeCache;
    private final EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> mapper;
    
    public EcoCodeSaveAdapter(JpaRepository<EcoIndCodeEntity, Long> repo, EcoIndCodeCache codeCache, EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> mapper) {
        super(repo);
        this.codeCache = codeCache;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<EcoIndCodeEntity> list) {
        repo.saveAll(list).forEach(ind->{
            codeCache.put(mapper.toDomain(ind), ind.getId());
        });
        flush();
    }
}
