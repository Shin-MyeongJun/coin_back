package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;

import java.util.List;

public interface GetPremiumSnapshotByBasePort {
    List<PremiumSnapshotView> findByBase(String base);
}
