package com.example.demo.alert.infrastructure.web;

import com.example.demo.alert.application.port.in.DeleteAlertRuleUseCase;
import com.example.demo.alert.application.port.in.QueryAlertRuleUseCase;
import com.example.demo.alert.application.port.in.RegisterAlertRuleUseCase;
import com.example.demo.alert.application.port.in.ToggleAlertRuleUseCase;
import com.example.demo.alert.application.port.in.UpdateAlertRuleUseCase;
import com.example.demo.alert.application.usecase.AlertRulePage;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.infrastructure.web.dto.AlertRuleRequest;
import com.example.demo.alert.infrastructure.web.dto.AlertRuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alert/rules")
@RequiredArgsConstructor
public class AlertRuleController {
    private final RegisterAlertRuleUseCase registerAlertRuleUseCase;
    private final UpdateAlertRuleUseCase updateAlertRuleUseCase;
    private final DeleteAlertRuleUseCase deleteAlertRuleUseCase;
    private final ToggleAlertRuleUseCase toggleAlertRuleUseCase;
    private final QueryAlertRuleUseCase queryAlertRuleUseCase;

    @GetMapping
    public RulePageResponse list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AlertRulePage result = queryAlertRuleUseCase.findByUser(resolveUserId(userId), page, size);
        List<AlertRuleResponse> items = result.items().stream()
                .map(AlertRuleResponse::from)
                .toList();
        return new RulePageResponse(items, result.page(), result.size(), result.total());
    }

    @PostMapping
    public ResponseEntity<AlertRuleResponse> register(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody AlertRuleRequest request
    ) {
        AlertRule saved = registerAlertRuleUseCase.register(resolveUserId(userId), request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/alert/rules/" + saved.getId()))
                .body(AlertRuleResponse.from(saved));
    }

    @GetMapping("/{id}")
    public AlertRuleResponse get(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable long id
    ) {
        return AlertRuleResponse.from(queryAlertRuleUseCase.get(resolveUserId(userId), id));
    }

    @PutMapping("/{id}")
    public AlertRuleResponse update(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable long id,
            @Valid @RequestBody AlertRuleRequest request
    ) {
        return AlertRuleResponse.from(updateAlertRuleUseCase.update(resolveUserId(userId), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable long id
    ) {
        deleteAlertRuleUseCase.delete(resolveUserId(userId), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public AlertRuleResponse toggle(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable long id
    ) {
        return AlertRuleResponse.from(toggleAlertRuleUseCase.toggle(resolveUserId(userId), id));
    }

    private String resolveUserId(String userId) {
        return userId == null || userId.isBlank() ? "anonymous" : userId;
    }

    public record RulePageResponse(
            List<AlertRuleResponse> items,
            int page,
            int size,
            long total
    ) {
    }
}
