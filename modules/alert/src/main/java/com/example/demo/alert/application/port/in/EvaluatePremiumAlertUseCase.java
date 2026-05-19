package com.example.demo.alert.application.port.in;

import com.example.demo.contracts.message.price_value.PremiumMessage;

public interface EvaluatePremiumAlertUseCase {
    void evaluate(PremiumMessage message);
}
