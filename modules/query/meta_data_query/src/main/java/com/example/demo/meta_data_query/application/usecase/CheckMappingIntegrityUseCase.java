package com.example.demo.meta_data_query.application.usecase;

import com.example.demo.meta_data_query.application.dto.MappingIntegrityResult;
import com.example.demo.meta_data_query.application.port.out.CheckMappingIntegrityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckMappingIntegrityUseCase {

    private final CheckMappingIntegrityPort port;

    public List<MappingIntegrityResult> execute() {
        return port.check();
    }
}
