package infrastructure.persistence.adapter.save;

import infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoScheduleSaveAdapter extends  EconomicSaveAdapter<EconomicScheduleEntity>  {
    public EcoScheduleSaveAdapter(JpaRepository<EconomicScheduleEntity, Long> repo) {
        super(repo);
    }

    @Override
    public void saveAll(List<EconomicScheduleEntity> list) {
        repo.saveAll(list);
        flush();
    }
}
