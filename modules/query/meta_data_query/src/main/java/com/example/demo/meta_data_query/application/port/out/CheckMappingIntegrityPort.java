package com.example.demo.meta_data_query.application.port.out;

import com.example.demo.meta_data_query.application.dto.MappingIntegrityResult;

import java.util.List;

public interface CheckMappingIntegrityPort {
    List<MappingIntegrityResult> check();
}
