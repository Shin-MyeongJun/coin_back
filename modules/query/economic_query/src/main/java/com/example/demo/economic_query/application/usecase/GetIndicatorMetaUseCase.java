package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.port.out.GetIndicatorMetaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetIndicatorMetaUseCase {

    private final GetIndicatorMetaPort port;

    public Optional<IndicatorMetaView> executeById(Long id) {
        return port.findById(id);
    }

    public List<IndicatorMetaView> executeAll() {
        return port.findAll();
    }
}
