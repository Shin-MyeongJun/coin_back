package infrastructure.persistence.adapter;

import application.port.out.FlushAndSaveEconomicValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@RequiredArgsConstructor
public abstract class EconomicSaveAdapter<ENTITY> implements FlushAndSaveEconomicValuePort<ENTITY> {
    protected  final JpaRepository<ENTITY, Long> repo;

    @Override
    public void flush() {
        repo.flush();
    }

    @Override
    public abstract void saveAll(List<ENTITY> list);
}
