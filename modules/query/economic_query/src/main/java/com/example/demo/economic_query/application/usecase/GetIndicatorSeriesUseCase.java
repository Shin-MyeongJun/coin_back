package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.application.port.out.GetIndicatorSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIndicatorSeriesUseCase {

    private final GetIndicatorSeriesPort port;

    public List<IndicatorSeriesView> execute(Long indCodeId, Long from, Long to) {
        return port.findByIndCodeIdAndDateRange(indCodeId, from, to);
    }
}
