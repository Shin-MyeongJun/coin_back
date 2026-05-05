package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetTickLatestIndicatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetTickLatestIndicatorUseCase {

    private final GetTickLatestIndicatorPort port;

    public Optional<TickIndicatorView> execute(Long marketCodeId, String interval, String type) {
        return port.findLatest(marketCodeId, interval, type);
    }
}
