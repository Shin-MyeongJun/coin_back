package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.port.out.GetIndicatorListByCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIndicatorListByCategoryUseCase {

    private final GetIndicatorListByCategoryPort port;

    public List<IndicatorMetaView> execute(String type) {
        return port.findByType(type);
    }
}
