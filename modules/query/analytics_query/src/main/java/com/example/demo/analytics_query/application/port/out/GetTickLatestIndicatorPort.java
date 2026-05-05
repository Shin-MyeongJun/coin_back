package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;

import java.util.Optional;

public interface GetTickLatestIndicatorPort {
    Optional<TickIndicatorView> findLatest(Long marketCodeId, String interval, String type);
}
