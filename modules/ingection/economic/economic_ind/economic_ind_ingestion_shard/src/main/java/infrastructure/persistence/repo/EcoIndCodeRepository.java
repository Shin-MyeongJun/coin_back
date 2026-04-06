package infrastructure.persistence.repo;

import infrastructure.persistence.entity.EcoIndCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcoIndCodeRepository extends JpaRepository<EcoIndCodeEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
}
