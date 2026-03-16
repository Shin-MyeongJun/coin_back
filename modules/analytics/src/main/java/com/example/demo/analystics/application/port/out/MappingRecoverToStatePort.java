package com.example.demo.analystics.application.port.out;

import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryState;

public interface MappingRecoverToStatePort<RECOVER extends RecoveryState,KEY extends DataKey<KEY>,STATE> {
    STATE toState(KEY key, RECOVER recover);
}
