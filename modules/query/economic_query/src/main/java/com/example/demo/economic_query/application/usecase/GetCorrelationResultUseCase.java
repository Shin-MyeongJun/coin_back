package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.CorrelationResultView;
import com.example.demo.economic_query.application.port.out.GetCorrelationResultPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCorrelationResultUseCase {

    private final GetCorrelationResultPort port;

    public List<CorrelationResultView> execute(String assetSymbol) {
        return port.findByAssetSymbol(assetSymbol);
    }
}
