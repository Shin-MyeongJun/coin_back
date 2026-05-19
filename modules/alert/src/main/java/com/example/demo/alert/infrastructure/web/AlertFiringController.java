package com.example.demo.alert.infrastructure.web;

import com.example.demo.alert.application.port.in.QueryAlertFiringUseCase;
import com.example.demo.alert.application.usecase.AlertFiringCursorPage;
import com.example.demo.alert.infrastructure.web.dto.AlertFiringResponse;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert/firings")
@RequiredArgsConstructor
public class AlertFiringController {
    private final QueryAlertFiringUseCase queryAlertFiringUseCase;

    @GetMapping
    public FiringCursorResponse list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit
    ) {
        AlertFiringCursorPage result = queryAlertFiringUseCase.findByUser(account.id().asString(), cursor, limit);
        List<AlertFiringResponse> items = result.items().stream()
                .map(AlertFiringResponse::from)
                .toList();
        return new FiringCursorResponse(items, result.nextCursor(), result.hasMore());
    }

    public record FiringCursorResponse(
            List<AlertFiringResponse> items,
            Long nextCursor,
            boolean hasMore
    ) {
    }
}
