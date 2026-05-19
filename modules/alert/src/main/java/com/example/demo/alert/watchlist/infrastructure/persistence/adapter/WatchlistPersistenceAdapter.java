package com.example.demo.alert.watchlist.infrastructure.persistence.adapter;

import com.example.demo.alert.watchlist.application.port.out.DeleteWatchlistPort;
import com.example.demo.alert.watchlist.application.port.out.LoadWatchlistPort;
import com.example.demo.alert.watchlist.application.port.out.SaveWatchlistPort;
import com.example.demo.alert.watchlist.application.usecase.WatchlistPage;
import com.example.demo.alert.watchlist.domain.domain.DuplicateWatchlistItemException;
import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;
import com.example.demo.alert.watchlist.infrastructure.persistence.entity.WatchlistItemEntity;
import com.example.demo.alert.watchlist.infrastructure.persistence.mapper.WatchlistItemEntityMapper;
import com.example.demo.alert.watchlist.infrastructure.persistence.repo.WatchlistItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WatchlistPersistenceAdapter implements LoadWatchlistPort, SaveWatchlistPort, DeleteWatchlistPort {
    private final WatchlistItemJpaRepository repository;
    private final WatchlistItemEntityMapper mapper;

    @Override
    public Optional<WatchlistItem> findByIdForUser(long id, String userId) {
        return repository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public WatchlistPage searchByUser(String userId, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("id"))
        );
        Page<WatchlistItemEntity> result = (keyword == null || keyword.isBlank())
                ? repository.findByUserId(userId, pageRequest)
                : repository.findByUserIdAndSymbolContainingIgnoreCase(userId, keyword.trim(), pageRequest);
        List<WatchlistItem> items = result.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new WatchlistPage(items, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    public WatchlistItem save(WatchlistItem item) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(item)));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateWatchlistItemException(
                    "watchlist already contains marketCodeId=" + item.marketCodeId() + " for user",
                    ex
            );
        }
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
