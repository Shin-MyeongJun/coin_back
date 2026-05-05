package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;
import com.example.demo.economic_query.application.port.out.GetIndicatorChangeRatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIndicatorChangeRateUseCase {

    private final GetIndicatorChangeRatePort port;

    public List<IndicatorChangeRateView> execute(Long indCodeId) {
        return port.findByIndCodeId(indCodeId);
    }
}
