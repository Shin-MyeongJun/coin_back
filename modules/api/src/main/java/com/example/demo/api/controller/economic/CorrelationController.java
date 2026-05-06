package com.example.demo.api.controller.economic;

import com.example.demo.economic_query.application.dto.CorrelationResultView;
import com.example.demo.economic_query.application.usecase.GetCorrelationResultUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/economic")
@RequiredArgsConstructor
public class CorrelationController {

    private final GetCorrelationResultUseCase getCorrelationResultUseCase;

    @GetMapping("/correlation")
    public List<CorrelationResultView> getCorrelation(@RequestParam String asset) {
        return getCorrelationResultUseCase.execute(asset);
    }
}
