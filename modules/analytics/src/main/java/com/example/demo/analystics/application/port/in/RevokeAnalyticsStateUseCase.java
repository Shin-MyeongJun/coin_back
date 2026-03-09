package com.example.demo.analystics.application.port.in;

import java.util.List;

public interface RevokeAnalyticsStateUseCase {
    void revoke(List<Integer> partitionIds);
}
