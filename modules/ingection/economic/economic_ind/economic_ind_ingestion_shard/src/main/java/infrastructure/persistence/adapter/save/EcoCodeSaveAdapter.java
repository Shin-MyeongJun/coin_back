package infrastructure.persistence.adapter.save;

import infrastructure.cache.EcoIndCodeCache;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoCodeSaveAdapter extends EconomicSaveAdapter<EcoIndCodeEntity> {
    
    private final EcoIndCodeCache codeCache;
    
    public EcoCodeSaveAdapter(JpaRepository<EcoIndCodeEntity, Long> repo, EcoIndCodeCache codeCache) {
        super(repo);
        this.codeCache = codeCache;
    }

    @Override
    public void saveAll(List<EcoIndCodeEntity> list) {
        repo.saveAll(list).forEach(ind->{
            codeCache.put(ind.getIndicatorCode(), ind.getId());
        });
        flush();
    }
}
