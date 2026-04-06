package infrastructure.persistence.repo;

import infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomicScheduleRepository extends JpaRepository<EconomicScheduleEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
}
