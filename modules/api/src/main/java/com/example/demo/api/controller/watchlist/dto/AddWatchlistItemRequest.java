package com.example.demo.api.controller.watchlist.dto;

import com.example.demo.alert.watchlist.application.usecase.AddWatchlistItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddWatchlistItemRequest(
        @NotNull
        @Min(1)
        Long marketCodeId,

        @Size(max = 32)
        String symbol,

        Long domesticExchangeId,

        Long offshoreExchangeId,

        @Min(0)
        Integer displayOrder,

        @Size(max = 255)
        String memo
) {
    public AddWatchlistItemCommand toCommand() {
        return new AddWatchlistItemCommand(
                marketCodeId,
                symbol,
                domesticExchangeId,
                offshoreExchangeId,
                displayOrder,
                memo
        );
    }
}
