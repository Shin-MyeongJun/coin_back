package com.example.demo.api.controller.meta;

import com.example.demo.meta_data_query.application.dto.MappingIntegrityResult;
import com.example.demo.meta_data_query.application.usecase.CheckMappingIntegrityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
public class MetaIntegrityController {

    private final CheckMappingIntegrityUseCase checkMappingIntegrityUseCase;

    @GetMapping("/integrity")
    public List<MappingIntegrityResult> checkIntegrity() {
        return checkMappingIntegrityUseCase.execute();
    }
}
