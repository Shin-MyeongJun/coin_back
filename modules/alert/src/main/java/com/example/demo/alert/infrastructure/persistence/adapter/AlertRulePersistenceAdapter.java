package com.example.demo.alert.infrastructure.persistence.adapter;

import com.example.demo.alert.application.port.out.DeleteAlertRulePort;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.application.port.out.SaveAlertRulePort;
import com.example.demo.alert.application.usecase.AlertRulePage;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.alert.infrastructure.persistence.mapper.AlertRuleEntityMapper;
import com.example.demo.alert.infrastructure.persistence.repo.AlertRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AlertRulePersistenceAdapter implements LoadAlertRulePort, SaveAlertRulePort, DeleteAlertRulePort {
    private final AlertRuleJpaRepository repository;
    private final AlertRuleEntityMapper mapper;

    @Override
    public Optional<AlertRule> findByIdForUser(long id, String userId) {
        return repository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public AlertRulePage findByUser(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<AlertRule> result = repository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest)
                .map(mapper::toDomain);
        return new AlertRulePage(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    public List<AlertRule> findAllActive() {
        return repository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertRule> findActiveByTargetAndAsset(TargetType targetType, String assetSymbol) {
        return repository.findByTargetTypeAndAssetSymbolAndActiveTrue(targetType.name(), assetSymbol).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public AlertRule save(AlertRule alertRule) {
        return mapper.toDomain(repository.save(mapper.toEntity(alertRule)));
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
