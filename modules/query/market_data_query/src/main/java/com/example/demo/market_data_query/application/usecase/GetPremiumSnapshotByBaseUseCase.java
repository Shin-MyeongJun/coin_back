package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.port.out.GetPremiumSnapshotByBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumSnapshotByBaseUseCase {

    private final GetPremiumSnapshotByBasePort port;

    public List<PremiumSnapshotView> execute(String base) {
        return port.findByBase(base);
    }
}
