package com.example.demo.alert.infrastructure.web;

import com.example.demo.alert.application.port.in.DeleteAlertRuleUseCase;
import com.example.demo.alert.application.port.in.QueryAlertRuleUseCase;
import com.example.demo.alert.application.port.in.RegisterAlertRuleUseCase;
import com.example.demo.alert.application.port.in.ToggleAlertRuleUseCase;
import com.example.demo.alert.application.port.in.UpdateAlertRuleUseCase;
import com.example.demo.alert.domain.exception.AlertRuleNotFoundException;
import com.example.demo.alert.infrastructure.config.AlertProperties;
import com.example.demo.alert.infrastructure.web.exception.AlertExceptionHandler;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlertRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AlertExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
class AlertRuleControllerErrorTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean RegisterAlertRuleUseCase registerAlertRuleUseCase;
    @MockitoBean UpdateAlertRuleUseCase updateAlertRuleUseCase;
    @MockitoBean DeleteAlertRuleUseCase deleteAlertRuleUseCase;
    @MockitoBean ToggleAlertRuleUseCase toggleAlertRuleUseCase;
    @MockitoBean QueryAlertRuleUseCase queryAlertRuleUseCase;
    @MockitoBean AlertProperties alertProperties;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor authenticated(AuthenticatedAccount account) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(account, "AT", "ROLE_USER"));
            return request;
        };
    }

    @Test
    void update_missing_rule_returns_404_problem_detail() throws Exception {
        // given
        AuthenticatedAccount me = new AuthenticatedAccount(AccountId.generate(), Email.of("a@b.com"), AccountTier.FREE);
        given(updateAlertRuleUseCase.update(any(), eq(999L), any()))
                .willThrow(new AlertRuleNotFoundException(999L));

        // when / then
        mvc.perform(put("/api/v1/alert/rules/999")
                        .with(authenticated(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "label", "my rule",
                                "targetType", "PREMIUM",
                                "assetSymbol", "BTC",
                                "operator", "GTE",
                                "threshold", 5.0,
                                "cooldownSec", 60,
                                "channels", List.of("SSE")
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Alert Rule Not Found"))
                .andExpect(jsonPath("$.detail").value("alert rule not found: 999"));
    }
}
