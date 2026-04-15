package infrastructure.persistence.adapter.read;

import application.port.out.ReadEcoIndCodePort;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EcoIndReadAdapter implements ReadEcoIndCodePort {

    JpaRepository<EcoIndCodeEntity, Long> repo;


    @Override
    public List<EcoIndCodeEntity> readAll() {
        return repo.findAll();
    }
}
