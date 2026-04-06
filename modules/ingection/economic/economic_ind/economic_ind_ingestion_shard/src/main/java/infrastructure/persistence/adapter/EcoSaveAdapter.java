package infrastructure.persistence.adapter;

import domain.EconomicRawIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EcoSaveAdapter extends EconomicSaveAdapter<EconomicRawIndicator> {
    public EcoSaveAdapter(JpaRepository<EconomicRawIndicator, Long> repo) {
        super(repo);
    }

    @Override
    public void saveAll(List<EconomicRawIndicator> list) {
        repo.saveAll(list);
    }
}
