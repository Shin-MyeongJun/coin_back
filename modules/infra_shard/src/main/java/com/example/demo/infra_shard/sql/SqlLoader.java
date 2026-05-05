package com.example.demo.infra_shard.sql;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Component
public class SqlLoader {

    public String load(String classpathPath) {
        try {
            return new ClassPathResource(classpathPath)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("SQL 파일 로드 실패: " + classpathPath, e);
        }
    }
}
