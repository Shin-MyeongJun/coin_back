package infrastructure.persistence.repo;

import infrastructure.persistence.entity.EcoIndEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcoIndRepository extends JpaRepository<EcoIndEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
}
