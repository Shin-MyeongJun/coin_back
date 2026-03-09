package com.example.demo.meta_data.infrastructure.runner;

import com.example.demo.meta_data.application.port.in.InitializeMetaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetaInitRunner  implements ApplicationRunner {

    private final List<InitializeMetaUseCase> initializers;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializers.forEach(InitializeMetaUseCase::initSend);
    }
}
