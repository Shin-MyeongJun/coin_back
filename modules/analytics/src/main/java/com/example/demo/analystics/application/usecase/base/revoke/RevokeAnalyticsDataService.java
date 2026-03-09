package com.example.demo.analystics.application.usecase.base.revoke;

import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;

import java.util.List;

public abstract class RevokeAnalyticsDataService implements RevokeAnalyticsStateUseCase {

    @Override
    public void revoke(List<Integer> partitionIds){

    }
}
