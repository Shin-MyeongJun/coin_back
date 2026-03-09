package com.example.demo.meta_data.application.port.out;

import java.util.Optional;

public interface FindAndSavePort<ENTITY,KEY> {
    Optional<ENTITY> findByKey(KEY key);
    ENTITY save(ENTITY entity);

}
