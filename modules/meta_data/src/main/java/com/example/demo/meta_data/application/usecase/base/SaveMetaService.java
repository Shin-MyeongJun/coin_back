package com.example.demo.meta_data.application.usecase.base;

import com.example.demo.meta_data.application.port.in.HandleSaveMetaUseCase;
import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.infrastructure.persistence.entity.HasMetaKey;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SaveMetaService<ENTITY extends HasMetaKey<KEY> ,KEY> implements HandleSaveMetaUseCase<ENTITY> {

    private final FindAndSavePort<ENTITY,KEY> repo;

    @Override
    public ENTITY handle(ENTITY entity) {
        Optional<ENTITY> en = repo.findByKey(entity.getKey());
        if(en.isPresent()){
            return repo.findByKey(entity.getKey()).get();
        }
        return repo.save(entity);
    }
}
